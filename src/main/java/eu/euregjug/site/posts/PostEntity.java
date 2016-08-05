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
package eu.euregjug.site.posts;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.Calendar;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotBlank;

/**
 * @author Michael J. Simons, 2015-12-28
 */
@Entity
@Table(
        name = "posts",
        uniqueConstraints = {
            @UniqueConstraint(name = "posts_uk", columnNames = {"published_on", "slug"})
        }
)
@NamedEntityGraph(name = "PostEntity.linkable",
        attributeNodes = {
            @NamedAttributeNode("publishedOn"),
            @NamedAttributeNode("slug"),
            @NamedAttributeNode("title")}
)
@NamedQueries({
        // Named query for older posts relative to the current
        @NamedQuery(name = "PostEntity.getPrevious",
                query
                = "Select p1 from PostEntity p1, PostEntity p2 "
                + " where p2.id = :id "
                + "   and p1.id <> p2.id "
                + "   and (p1.publishedOn < p2.publishedOn or (p1.publishedOn = p2.publishedOn and p1.createdAt < p2.createdAt)) "
                + " order by p1.publishedOn desc, p1.createdAt desc "
        ),
        // Named query for newer posts relative to the current
        @NamedQuery(name = "PostEntity.getNext",
                query
                = "Select p1 from PostEntity p1, PostEntity p2 "
                + " where p2.id = :id "
                + "   and p1.id <> p2.id "
                + "   and (p1.publishedOn > p2.publishedOn or (p1.publishedOn = p2.publishedOn and p1.createdAt > p2.createdAt)) "
                + " order by p1.publishedOn asc, p1.createdAt asc "
        )
})
@JsonInclude(Include.NON_EMPTY)
@EqualsAndHashCode(of = {"publishedOn", "slug"})
public class PostEntity implements Serializable {

    private static final long serialVersionUID = -2488354242899068540L;

    /**
     * Textformat of a post.
     */
    public enum Format {

        asciidoc, markdown
    }

    /**
     * Primary key of this post.
     */
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    @Getter(onMethod = @__(@JsonProperty))
    private Integer id;

    /**
     * Date when this post was or will be published.
     */
    @Column(name = "published_on", nullable = false)
    @Temporal(TemporalType.DATE)
    @NotNull
    @Getter
    private Date publishedOn;

    /**
     * A slug for this post to identify it. Can be generated or manually set.
     */
    @Column(name = "slug", length = 512, nullable = false)
    @Size(max = 512)
    @Getter
    private String slug;

    /**
     * The title of this post.
     */
    @Column(name = "title", length = 512, nullable = false)
    @NotBlank
    @Size(max = 512)
    @Getter @Setter
    private String title;

    /**
     * Content of this posts.
     */
    @Column(name = "content", nullable = false)
    @Lob
    @Basic(fetch = FetchType.EAGER)
    @NotBlank
    @Getter @Setter
    private String content;

    /**
     * Format of this posts. Defaults to Markdown.
     */
    @Column(name = "format")
    @Enumerated(EnumType.STRING)
    @NotNull
    @Getter @Setter
    private Format format = Format.asciidoc;

    /**
     * Creation date of this post.
     */
    @Column(name = "created_at", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @JsonIgnore
    @Getter
    private Calendar createdAt;

    /**
     * Last update to this post.
     */
    @Column(name = "updated_at", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @JsonIgnore
    @Getter
    private Calendar updatedAt;

    /**
     * Needed for Hibernate, not to be called by application code.
     */
    @SuppressWarnings({"squid:S2637"})
    protected PostEntity() {
    }

    public PostEntity(final Date publishedOn, final String slug, final String title, final String content) {
        this.publishedOn = publishedOn;
        this.slug = slug;
        this.title = title;
        this.content = content;
        this.createdAt = Calendar.getInstance();
    }

    final String generateSlug(final String suggestedSlug, final String newTitle) {
        String rv = suggestedSlug;
        if (rv == null || rv.trim().isEmpty()) {
            rv = Normalizer.normalize(newTitle.toLowerCase(), Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}|[^\\w\\s]", "").replaceAll("[\\s-]+", " ").trim().replaceAll("\\s", "-");
        }
        return rv;
    }

    /**
     * Updates the {@link #updatedAt} timestamp
     */
    @PrePersist
    @PreUpdate
    void updateUpdatedAt() {
        if (this.createdAt == null) {
            this.createdAt = Calendar.getInstance();
        }
        this.slug = generateSlug(slug, title);
        this.updatedAt = Calendar.getInstance();
    }
}
