/*
 * Copyright 2016 EuregJUG.
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
import eu.euregjug.site.config.SecurityTestConfig;
import java.io.InputStream;
import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.mock.web.MockMultipartFile;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Michael J. Simons, 2016-07-15
 */
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@WebMvcTest(AssetApiController.class)
@EnableSpringDataWebSupport // Needed to enable resolving of Pageable and other parameters
@Import(SecurityTestConfig.class) // Needed to get rid of default CSRF protection
@AutoConfigureRestDocs(
        outputDir = "target/generated-snippets",
        uriHost = "euregjug.eu",
        uriPort = 80
)
public class AssetApiControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private GridFsTemplate gridFsTemplate;

    @Test
    public void createShouldThrowException() throws Exception {
        final MockMultipartFile multipartFile = new MockMultipartFile("assetData", this.getClass().getResourceAsStream("/eu/euregjug/site/assets/asset.png"));
        when(this.gridFsTemplate.findOne(any(Query.class))).thenReturn(mock(GridFSDBFile.class));

        mvc
                .perform(
                        fileUpload("/api/assets")
                        .file(multipartFile)
                )
                .andExpect(status().isConflict())
                .andExpect(content().string(""));

        verify(this.gridFsTemplate).findOne(any(Query.class));
        verifyNoMoreInteractions(this.gridFsTemplate);
    }

    @Test
    public void createShouldWork() throws Exception {
        final MockMultipartFile multipartFile = new MockMultipartFile("assetData", "asset.png", null, this.getClass().getResourceAsStream("/eu/euregjug/site/assets/asset.png"));

        when(this.gridFsTemplate.findOne(any(Query.class))).thenReturn(null);

        mvc
                .perform(
                        fileUpload("/api/assets")
                        .file(multipartFile)
                )
                .andExpect(status().isCreated())
                .andExpect(content().string("asset.png"))
                .andDo(document("api/assets/create",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())
                ));

        verify(this.gridFsTemplate).findOne(any(Query.class));
        verify(this.gridFsTemplate).store(any(InputStream.class), eq("asset.png"), eq("image/png"));
        verifyNoMoreInteractions(this.gridFsTemplate);
    }
}
