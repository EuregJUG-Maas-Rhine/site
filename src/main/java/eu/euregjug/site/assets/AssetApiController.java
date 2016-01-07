/*
 * Copyright 2015 EuregJUG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.euregjug.site.assets;

import com.mongodb.gridfs.GridFSDBFile;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.tika.Tika;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.mime.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import static java.time.ZoneId.of;
import static java.time.ZonedDateTime.now;
import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * @author Michael J. Simons, 2015-12-29
 */
@Controller
@RequestMapping("/api/assets")
public class AssetApiController {

    public static final Logger logger = LoggerFactory.getLogger(AssetApiController.class.getPackage().getName());

    private final Tika tika;

    private final GridFsTemplate gridFs;

    @Autowired
    public AssetApiController(GridFsTemplate gridFs) {
	this.tika = new Tika();
	this.gridFs = gridFs;
    }

    @RequestMapping(method = POST)
    @PreAuthorize("isAuthenticated()")
    public @ResponseBody
    String create(
	    final @RequestParam("assetData") MultipartFile assetData
    ) throws IOException {

	// Check duplicates
	final GridFSDBFile file = this.gridFs.findOne(Query.query(Criteria.where("filename").is(assetData.getOriginalFilename())));
	if(file != null) {
	    throw new DataIntegrityViolationException(String.format("Asset with name '%s' already exists", assetData.getOriginalFilename()));
	} else {
	    try (InputStream _in = TikaInputStream.get(assetData.getInputStream())) {
		MediaType mediaType = null;
		try {
		    mediaType = MediaType.parse(tika.detect(_in, assetData.getOriginalFilename()));
		} catch (IOException e) {
		    logger.warn("Could not detect content type", e);
		}
		this.gridFs.store(assetData.getInputStream(), assetData.getOriginalFilename(), Optional.ofNullable(mediaType).map(MediaType::toString).orElse(null));
		return assetData.getOriginalFilename();
	    }
	}
    }

    @RequestMapping({"/{filename:.+}"})
    public void get(
	    final @PathVariable String filename,
	    final HttpServletRequest request,
	    final HttpServletResponse response
    ) throws IOException {
	final GridFSDBFile file = this.gridFs.findOne(Query.query(Criteria.where("filename").is(filename)));
	if (file == null) {
	    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
	} else {
	    final int cacheForDays = 365;
	    response.setHeader("Content-Type", file.getContentType());
	    response.setHeader("Content-Disposition", String.format("inline; filename=\"%s\"", file.getFilename()));
	    response.setHeader("Expires", now(of("UTC")).plusDays(cacheForDays).format(RFC_1123_DATE_TIME));
	    response.setHeader("Cache-Control", String.format("max-age=%d, %s", TimeUnit.DAYS.toSeconds(cacheForDays), "public"));
	    file.writeTo(response.getOutputStream());
	    response.flushBuffer();
	}
    }
}
