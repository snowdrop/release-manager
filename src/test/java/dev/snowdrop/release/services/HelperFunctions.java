/**
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package dev.snowdrop.release.services;

import javax.inject.Inject;
import java.io.InputStream;
import java.util.Scanner;

/**
 * @author <a href="antcosta@redhat.com">Antonio Costa</a>
 */
public class HelperFunctions {

    @Inject
    JiraIssueFactory factory;

    public static InputStream getResourceAsStream(String s) {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(s);
    }

    public static String getStreamContents(InputStream inputStream) {
        String contents = "";
        @SuppressWarnings("resource")
        Scanner s = new Scanner(inputStream);
        s.useDelimiter("\\A");
        if (s.hasNext()) {
            contents = s.next();
        }
        return contents;
    }


}