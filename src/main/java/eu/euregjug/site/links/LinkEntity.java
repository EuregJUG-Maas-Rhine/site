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
package eu.euregjug.site.links;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

/**
 * @author Michael J. Simons, 2015-12-27
 */
@Entity
@Table(name = "links")
public class LinkEntity implements Serializable {

    private static final long serialVersionUID = 2624180625891214911L;

    /**
     * Types of links
     */
    public static enum Type {

	generic, profile, sponsor
    }

    /**
     * Primary key of this link.
     */
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "links_id_seq_generator")
    @SequenceGenerator(name = "links_id_seq_generator", sequenceName = "links_id_seq")
    @JsonIgnore
    private Integer id;

    /**
     * Type of the link.
     */
    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    @NotNull
    private Type type = Type.generic;
    
    /**
     * Target of the link.
     */
    @Column(name = "target", length = 1024, nullable = false, unique = true)
    @URL
    @Size(max = 1024)
    private String target;
    
    /**
     * Title of the link.
     */
    @Column(name = "title", length = 512, nullable = false)    
    @Size(max = 512)
    private String title;
    
    @Column(name = "sort_col", nullable = false)    
    private Integer sortCol = 0;
    
    /**
     * An optional font-awesome or similar icon.
     */
    @Column(name = "icon", length = 128)    
    @Size(max = 128)
    private String icon;
    
    /**
     * An optional, local image resoure relative to {@code /img}. 
     */
    @Column(name = "local_image_resource", length = 128)    
    @Size(max = 128)
    private String localImageResource;

    /**
     * Needed for Hibernate, not to be called by application code.
     */
    @SuppressWarnings({"squid:S2637"})
    protected LinkEntity() {
    }

    public LinkEntity(String target, String title) {
	this.target = target;
	this.title = title;
    }

    @JsonProperty
    public Integer getId() {
	return id;
    }
    
    public Type getType() {
	return type;
    }

    public void setType(Type type) {
	this.type = type;
    }

    public String getTarget() {
	return target;
    }

    public String getTitle() {
	return title;
    }

    public void setTitle(String title) {
	this.title = title;
    }

    public Integer getSortCol() {
	return sortCol;
    }

    public void setSortCol(Integer sortCol) {
	this.sortCol = sortCol;
    }
    
    public String getIcon() {
	return icon;
    }

    public void setIcon(String icon) {
	this.icon = icon;
    }

    public String getLocalImageResource() {
	return localImageResource;
    }

    public void setLocalImageResource(String localImageResource) {
	this.localImageResource = localImageResource;
    }

    @Override
    public int hashCode() {
	int hash = 7;
	hash = 29 * hash + Objects.hashCode(this.target);
	return hash;
    }

    @Override
    public boolean equals(Object obj) {
	if (this == obj) {
	    return true;
	}
	if (obj == null) {
	    return false;
	}
	if (getClass() != obj.getClass()) {
	    return false;
	}
	final LinkEntity other = (LinkEntity) obj;
	return Objects.equals(this.target, other.target);
    }
}
