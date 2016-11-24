/*
 * Copyright 2016 Realm Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.realm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * A User Store backed by the ObjectStore metadata Realm to store user.
 */
class ObjectStoreUserStore implements UserStore {
    ObjectStoreUserStore (String path, byte[] aesKey) {
        configureMetaDataSystem(path, aesKey);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SyncUser put(SyncUser user) {
        String userJson = user.toJson();
        // create or update token (userJson) using identity
        updateOrCreateUser(user.getIdentity(), userJson, user.getSyncUser().getAuthenticationUrl().toString());
        return user;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SyncUser get() {
        String userJson = getCurrentUser();
        if (userJson != null) {
            return SyncUser.fromJson(userJson);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void remove() {
        logoutCurrentUser();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<SyncUser> allUsers() {
        String[] allUsers = getAllUsers();
        if (allUsers != null && allUsers.length > 0) {
            ArrayList<SyncUser> users = new ArrayList<SyncUser>(allUsers.length);
            for (String userJson : allUsers) {
                users.add(SyncUser.fromJson(userJson));
            }
            return users;
        }
        return Collections.emptyList();
    }

    // init and load the Metadata Realm containing SyncUsers
    private static native void configureMetaDataSystem(String baseFile, byte[] aesKey);

    // return json data (token) of the current logged in user
    private static native String getCurrentUser ();

    private static native String[] getAllUsers();

    private static native void updateOrCreateUser(String identity, String jsonToken, String url);

    private static native void logoutCurrentUser ();

    // Should only be called for tests
    static native void reset_for_testing();

}
