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
package eu.euregjug.site.posts;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author Michael J. Simons, 2016-07-17
 */
@RunWith(SpringRunner.class)
@DataJpaTest
public class PostRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    @Test
    public void getPreviousShouldWork() {
        PostEntity currentPost = postRepository.findOne(3).get();
        currentPost = postRepository.getPrevious(currentPost).get();
        assertThat(currentPost.getId(), is(2));
        currentPost = postRepository.getPrevious(currentPost).get();
        assertThat(currentPost.getId(), is(1));
        assertThat(postRepository.getPrevious(currentPost).isPresent(), is(false));
    }

    @Test
    public void getNextShouldWork() {
        PostEntity currentPost = postRepository.findOne(3).get();
        currentPost = postRepository.getNext(currentPost).get();
        assertThat(currentPost.getId(), is(4));
        currentPost = postRepository.getNext(currentPost).get();
        assertThat(currentPost.getId(), is(5));
        assertThat(postRepository.getNext(currentPost).isPresent(), is(false));
    }
}
