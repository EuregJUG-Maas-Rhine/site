/*
 * Copyright 2018 EuregJUG.
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

package eu.euregjug.site.jobwall;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import static java.time.temporal.ChronoUnit.DAYS;
import java.util.Date;
import java.util.Optional;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

/**
 * Represents an employment ad.
 * @author Michael J. Simons, 2018-04-12
 */
@Entity
@Table(
        name = "employment_ads",
        uniqueConstraints = {
            @UniqueConstraint(name = "employment_ads_uk", columnNames = {"company", "posted_on", "title"})
        }
)
@JsonInclude(NON_NULL)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@EqualsAndHashCode(of = { "company", "postedOn", "title"})
public class AdEntity implements Serializable {
    private static final long serialVersionUID = -5267387497454954476L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    @Getter(onMethod = @__(@JsonProperty))
    private Integer id;

    /**
     * The company who posted this. Not normalized on purpose (i.e. lazyness).
     */
    @Column(nullable = false)
    @NotBlank
    @Getter
    private String company;

    /**
     * The date the ad was posted.
     */
    @Column(name = "posted_on", nullable = false)
    @Temporal(TemporalType.DATE)
    @NotNull
    @Getter
    private Date postedOn;

    /**
     * THe title of this add.
     */
    @Column(nullable = false)
    @NotBlank
    @Getter
    private String title;

    /**
     * The date until the ad is valid. Must be after {@link #postedOn}.
     */
    @Column(name = "valid_until", nullable = false)
    @Temporal(TemporalType.DATE)
    @NotNull
    @Getter
    private Date validUntil;

    /**
     * Content of this ad.
     */
    @Column(nullable = false)
    @Lob
    @Basic(fetch = FetchType.EAGER)
    @NotBlank
    @Getter @Setter
    private String content;

    /**
     * An optional direct link.
     */
    @Column(length = 512)
    @URL
    @Getter @Setter
    private String url;

    public AdEntity(final String company, final Date postedOn, final String title, final Date validUntil, final String content) {
        this.company = company;
        this.postedOn = postedOn;
        this.title = title;
        this.validUntil = Optional.ofNullable(validUntil)
                .orElseGet(() -> Date.from(postedOn.toInstant().plus(30, DAYS)));
        if (!this.postedOn.toInstant().isBefore(this.validUntil.toInstant())) {
            throw new IllegalArgumentException("Valid until must be a date after posted on!");
        }
        this.content = content;
    }
}
