/**
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.snowdrop.release.model;

import java.net.URI;

import javax.annotation.Nullable;

import com.atlassian.jira.rest.client.api.UserRestClient;
import com.atlassian.jira.rest.client.api.domain.User;
import com.atlassian.jira.rest.client.api.domain.input.UserInput;
import io.atlassian.util.concurrent.Promise;
import io.atlassian.util.concurrent.Promises;

/**
 * @author <a href="claprun@redhat.com">Christophe Laprun</a>
 */
public class MockUserRestClient implements UserRestClient {
    static final String NON_EXISTING_USER = "doesntexist";
    
    @Override
    public Promise<User> getUser(String username) {
        return NON_EXISTING_USER.equals(username) ? Promises.rejected(new RuntimeException("User name '" + username + "' not found")) : Promises.promise(null);
    }
    
    @Override
    public Promise<User> getUser(URI userUri) {
        return null;
    }
    
    @Override
    public Promise<User> createUser(UserInput userInput) {
        return null;
    }
    
    @Override
    public Promise<User> updateUser(URI userUri, UserInput userInput) {
        return null;
    }
    
    @Override
    public Promise<Void> removeUser(URI userUri) {
        return null;
    }
    
    @Override
    public Promise<Iterable<User>> findUsers(String username) {
        return null;
    }
    
    @Override
    public Promise<Iterable<User>> findUsers(String username, @Nullable Integer startAt, @Nullable Integer maxResults, @Nullable Boolean includeActive, @Nullable Boolean includeInactive) {
        return null;
    }
}
