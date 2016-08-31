/*
 * Copyright 2015-2016 EuregJUG.
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
package eu.euregjug.site.links;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

/**
 * @author Michael J. Simons, 2015-12-27
 */
@Entity
@Table(name = "links")
@EqualsAndHashCode(of = {"target"})
public class LinkEntity implements Serializable {

    private static final long serialVersionUID = 2624180625891214911L;

    /**
     * Types of links
     */
    public enum Type {

        generic, profile, sponsor
    }

    /**
     * Primary key of this link.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    @Getter(onMethod = @__(@JsonProperty))
    private Integer id;

    /**
     * Type of the link.
     */
    @Enumerated(EnumType.STRING)
    @NotNull
    @Getter @Setter
    private Type type = Type.generic;

    /**
     * Target of the link.
     */
    @Column(length = 1024, nullable = false, unique = true)
    @URL
    @Size(max = 1024)
    @Getter
    private String target;

    /**
     * Title of the link.
     */
    @Column(length = 512, nullable = false)
    @Size(max = 512)
    @Getter @Setter
    private String title;

    @Column(name = "sort_col", nullable = false)
    @Getter @Setter
    private Integer sortCol = 0;

    /**
     * An optional font-awesome or similar icon.
     */
    @Column(length = 128)
    @Size(max = 128)
    @Getter @Setter
    private String icon;

    /**
     * An optional, local image resoure relative to {@code /img}.
     */
    @Column(name = "local_image_resource", length = 128)
    @Size(max = 128)
    @Getter @Setter
    private String localImageResource;

    /**
     * Needed for Hibernate, not to be called by application code.
     */
    @SuppressWarnings({"squid:S2637"})
    protected LinkEntity() {
    }

    public LinkEntity(final String target, final String title) {
        this.target = target;
        this.title = title;
    }
}
