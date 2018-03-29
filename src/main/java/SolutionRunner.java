// Copyright 2018 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

import static com.google.api.ads.common.lib.utils.Builder.DEFAULT_CONFIGURATION_FILENAME;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.util.List;

import com.google.api.ads.adwords.axis.v201710.cm.ApiError;
import com.google.api.ads.adwords.axis.v201710.cm.ApiException;
import com.google.api.ads.adwords.lib.client.AdWordsSession;
import com.google.api.ads.common.lib.auth.OfflineCredentials;
import com.google.api.ads.common.lib.auth.OfflineCredentials.Api;
import com.google.api.ads.common.lib.conf.ConfigurationLoadException;
import com.google.api.ads.common.lib.exception.OAuthException;
import com.google.api.ads.common.lib.exception.ValidationException;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.content.ShoppingContent;
import com.google.api.services.content.ShoppingContentScopes;
import com.google.api.services.content.model.AccountIdentifier;
import com.google.common.base.Preconditions;

public class SolutionRunner {

  public static void main(String[] args) {
    ShoppingContent contentApiSession = null;
    try {
      HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
      JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
      try (InputStream s = Files.newInputStream(
          Paths.get(System.getProperty("user.home"),
          "shopping-samples", "content", "service-account.json"))) {
        Credential contentApiCredential = GoogleCredential.fromStream(s, httpTransport, jsonFactory)
            .createScoped(ShoppingContentScopes.all());
        if (!contentApiCredential.refreshToken()) {
          System.err.println("The service account access token could not be refreshed.");
          System.err.println("The service account key may have been deleted in the API Console.");
          return;
        }
        // TODO(sessions): Create a ShoppingContent object using ShoppingContent.Builder.
      }
    } catch (Exception e) {
      System.err.printf(
          "Failed to create service account credentials for Content API use. Exception: %s%n",
          e);
      return;
    }

    AdWordsSession adWordsSession = null;
    try {
      Credential adwordsOAuth2Credential =
          new OfflineCredentials.Builder()
              .forApi(Api.ADWORDS)
              .fromFile()
              .build()
              .generateCredential();
      // TODO(sessions): Create a AdWordsSession object using AdWordsSession.Builder.
    } catch (ConfigurationLoadException cle) {
      System.err.printf(
          "Failed to load configuration from the %s file. Exception: %s%n",
          DEFAULT_CONFIGURATION_FILENAME, cle);
      return;
    } catch (ValidationException ve) {
      System.err.printf(
          "Invalid configuration in the %s file. Exception: %s%n",
          DEFAULT_CONFIGURATION_FILENAME, ve);
      return;
    } catch (OAuthException oe) {
      System.err.printf(
          "Failed to create OAuth credentials. Check OAuth settings in the %s file. "
          + "Exception: %s%n",
          DEFAULT_CONFIGURATION_FILENAME, oe);
      return;
    }

    try {
      createAndLinkAccounts(adWordsSession, contentApiSession);
    } catch (ApiException apiException) {
      System.err.println("Request failed due to AdWords ApiException. Underlying ApiErrors:");
      if (apiException.getErrors() != null) {
        int i = 0;
        for (ApiError apiError : apiException.getErrors()) {
          System.err.printf("  Error %d: %s%n", i++, apiError);
        }
      }
    } catch (RemoteException re) {
      System.err.printf("Request failed unexpectedly due to AdWords RemoteException: %s%n", re);
    } catch (IOException ioe) {
      System.err.printf("Example failed due to Content API failure: %s%n", ioe);
    }
  }

  /**
   * Creates both an AdWords account and a Merchant Center account and links the two.
   *
   * @throws IllegalArgumentException if any of the method arguments are null
   * @throws IOException if anything goes wrong in either the creation or linking process
   */
  private static void createAndLinkAccounts(
      AdWordsSession adWordsSession, ShoppingContent contentApiSession) throws IOException {
    Preconditions.checkNotNull(adWordsSession, "No AdWords session object was provided");
    Preconditions.checkNotNull(contentApiSession, "No Content API session object was provided");

    // Retrieve the multi-client account in which the service account is a listed user. Assuming
    // that the service account is only attached to a single account (which will be the case if you
    // create a service account key from within Merchant Center) simplifies using this codelab,
    // as no further configuration is needed for the Content API side of things.
    BigInteger mcaId = getMcaId(contentApiSession);

    // First, we create an AdWords account. This allows us to use its customer ID to request
    // the link during creation of the corresponding Merchant Center account. Otherwise we'd need
    // to wait for the Merchant Center account to be fully created before we could request the link.

    // TODO(newAWaccount): Using the ManagedCustomerService, create a new testing AdWords account
    // under the given manager account.

    // Next, we create the Merchant Center account, specifying a single link to the AdWords account
    // we just created as part of that process.

    // TODO(newMCaccount): Using Accounts.insert, create a new Merchant Center account under the
    // given multi-client account that includes a (requested) link to the new AdWords account.

    // Finally, we accept the link on behalf of the new AdWords account, which completes the
    // linking process.

    // TODO(acceptLink): Using the mutateServiceLinks method in CustomerService, accept the
    // proposed link between the new AdWords account and the new Merchant Center account.
  }

  /**
   * Retrieves the Merchant Center multi-client account ID to which the authenticated client has
   * been added as a user.
   *
   * @throws IllegalArgumentException if the client is not a user on any MC accounts, is a user on
   *     multiple accounts, or the sole MC account is not a multi-client account
   * @throws IOException if there is a failure calling the Accounts.authinfo method
   */
  private static BigInteger getMcaId(ShoppingContent session) throws IOException {
    List<AccountIdentifier> ids = session.accounts().authinfo().execute().getAccountIdentifiers();
    Preconditions.checkArgument(
        !ids.isEmpty(),
        "Authenticated user does not have access to any Merchant Center accounts");
    Preconditions.checkArgument(
        ids.size() < 2,
        "Authenticated user has access to multiple Merchant Center accounts");
        BigInteger aggregatorId = ids.get(0).getAggregatorId();
        // For an MCA, only aggregatorId should be set. If merchantId is also set, then this is a
        // sub-account of an MCA, not an MCA itself.
        Preconditions.checkArgument(
            aggregatorId != null && ids.get(0).getMerchantId() == null,
            "Authenticated user does not have access to a multi-client account");
        return aggregatorId;
      }
}
