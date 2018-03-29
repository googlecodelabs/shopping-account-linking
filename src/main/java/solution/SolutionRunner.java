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

// MOE:begin_strip
// LAB(begin solution)
// MOE:end_strip
package solution;

// MOE:begin_strip
// LAB(end solution)
// MOE:end_strip
import static com.google.api.ads.common.lib.utils.Builder.DEFAULT_CONFIGURATION_FILENAME;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.util.List;
// MOE:begin_strip
// LAB(begin solution)
// MOE:end_strip
import java.util.Random;
// MOE:begin_strip
// LAB(end solution)
// MOE:end_strip

// MOE:begin_strip
// LAB(begin solution)
// MOE:end_strip
import com.google.api.ads.adwords.axis.factory.AdWordsServices;
import com.google.api.ads.adwords.axis.v201710.mcm.ServiceType;
// MOE:begin_strip
// LAB(end solution)
// MOE:end_strip
import com.google.api.ads.adwords.axis.v201710.cm.ApiError;
import com.google.api.ads.adwords.axis.v201710.cm.ApiException;
// MOE:begin_strip
// LAB(begin solution)
// MOE:end_strip
import com.google.api.ads.adwords.axis.v201710.cm.Operator;
import com.google.api.ads.adwords.axis.v201710.mcm.CustomerServiceInterface;
import com.google.api.ads.adwords.axis.v201710.mcm.ManagedCustomer;
import com.google.api.ads.adwords.axis.v201710.mcm.ManagedCustomerOperation;
import com.google.api.ads.adwords.axis.v201710.mcm.ManagedCustomerReturnValue;
import com.google.api.ads.adwords.axis.v201710.mcm.ManagedCustomerServiceInterface;
import com.google.api.ads.adwords.axis.v201710.mcm.ServiceLink;
import com.google.api.ads.adwords.axis.v201710.mcm.ServiceLinkLinkStatus;
import com.google.api.ads.adwords.axis.v201710.mcm.ServiceLinkOperation;
// MOE:begin_strip
// LAB(end solution)
// MOE:end_strip
import com.google.api.ads.adwords.lib.client.AdWordsSession;
// MOE:begin_strip
// LAB(begin solution)
// MOE:end_strip
import com.google.api.ads.adwords.lib.factory.AdWordsServicesInterface;
// MOE:begin_strip
// LAB(end solution)
// MOE:end_strip
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
// MOE:begin_strip
// LAB(begin solution)
// MOE:end_strip
import com.google.api.services.content.model.Account;
import com.google.api.services.content.model.AccountAdwordsLink;
// MOE:begin_strip
// LAB(end solution)
// MOE:end_strip
import com.google.api.services.content.model.AccountIdentifier;
// MOE:begin_strip
// LAB(begin solution)
// MOE:end_strip
import com.google.common.collect.ImmutableList;
// MOE:begin_strip
// LAB(end solution)
// MOE:end_strip
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
        // MOE:begin_strip
        // LAB(begin solution)
        // MOE:end_strip
        contentApiSession =
            new ShoppingContent.Builder(httpTransport, jsonFactory, contentApiCredential)
                .setApplicationName("Linking AdWords and Merchant Center Accounts Codelab")
                .build();
        // MOE:begin_strip
        // LAB(replace solution)
        // // TODO(sessions): Create a ShoppingContent object using ShoppingContent.Builder.
        // LAB(end solution)
        // MOE:end_strip
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
      // MOE:begin_strip
      // LAB(begin solution)
      // MOE:end_strip
      adWordsSession =
          new AdWordsSession.Builder()
              .fromFile()
              .withOAuth2Credential(adwordsOAuth2Credential)
              .build();
      // MOE:begin_strip
      // LAB(replace solution)
      // // TODO(sessions): Create a AdWordsSession object using AdWordsSession.Builder.
      // LAB(end solution)
      // MOE:end_strip
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

    // MOE:begin_strip
    // LAB(begin solution)
    // MOE:end_strip
    // Here, we create a random run number to use in the name of the new AdWords and Merchant
    // Center accounts. That way it's easy to tell which accounts should be linked if we want to
    // verify that the process worked.
    Random rand = new Random();
    long run = rand.nextLong();

    AdWordsServicesInterface adWordsServices = AdWordsServices.getInstance();

    ManagedCustomerServiceInterface managedCustomerService =
        adWordsServices.get(adWordsSession, ManagedCustomerServiceInterface.class);

    ManagedCustomer newAdWordsAccount = new ManagedCustomer();
    newAdWordsAccount.setName(String.format("AdWords Account Created by Run %d", run));
    newAdWordsAccount.setTestAccount(true);
    newAdWordsAccount.setCurrencyCode("USD");
    newAdWordsAccount.setDateTimeZone("America/Los_Angeles");

    ManagedCustomerOperation operation = new ManagedCustomerOperation();
    operation.setOperand(newAdWordsAccount);
    operation.setOperator(Operator.ADD);

    ManagedCustomerReturnValue result =
        managedCustomerService.mutate(new ManagedCustomerOperation[] {operation});
    Long adWordsId = result.getValue()[0].getCustomerId();
    System.out.printf("Created new AdWords account %d%n", adWordsId);
    // MOE:begin_strip
    // LAB(replace solution)
    // // TODO(newAWaccount): Using the ManagedCustomerService, create a new testing AdWords account
    // // under the given manager account.
    // LAB(end solution)
    // MOE:end_strip

    // Next, we create the Merchant Center account, specifying a single link to the AdWords account
    // we just created as part of that process.

    // MOE:begin_strip
    // LAB(begin solution)
    // MOE:end_strip
    Account newMcAccount = contentApiSession.accounts().insert(mcaId,
        new Account()
            .setName(String.format("Merchant Center Account Created by Run %d", run))
            .setAdwordsLinks(
                ImmutableList.of(
                    new AccountAdwordsLink()
                        .setAdwordsId(BigInteger.valueOf(adWordsId))
                        .setStatus("active")))).execute();

    System.out.printf("Created new Merchant Center account %s%n", newMcAccount.getId());
    // MOE:begin_strip
    // LAB(replace solution)
    // // TODO(newMCaccount): Using Accounts.insert, create a new Merchant Center account under the
    // // given multi-client account that includes a (requested) link to the new AdWords account.
    // LAB(end solution)
    // MOE:end_strip

    // Finally, we accept the link on behalf of the new AdWords account, which completes the
    // linking process.

    // MOE:begin_strip
    // LAB(begin solution)
    // MOE:end_strip
    // Modify the session to use the newly created account for the upcoming operations.
    adWordsSession.setClientCustomerId(adWordsId.toString());
    CustomerServiceInterface customerService =
        adWordsServices.get(adWordsSession, CustomerServiceInterface.class);

    // The link ID for a Merchant Center link is the ID of the Merchant Center that is requesting
    // the link, so instead of walking the pending links to find the right one, we can just
    // immediately approve the link.
    ServiceLinkOperation op = new ServiceLinkOperation();
    op.setOperator(Operator.SET);
    ServiceLink serviceLink = new ServiceLink();
    serviceLink.setServiceLinkId(newMcAccount.getId().longValue());
    serviceLink.setLinkStatus(ServiceLinkLinkStatus.ACTIVE);
    serviceLink.setServiceType(ServiceType.MERCHANT_CENTER);
    op.setOperand(serviceLink);

    ServiceLink[] mutatedServiceLinks =
        customerService.mutateServiceLinks(new ServiceLinkOperation[] {op});
    for (ServiceLink mutatedServiceLink : mutatedServiceLinks) {
      System.out.printf(
          "Service link with service link ID %d, type '%s' updated to status: %s.%n",
          mutatedServiceLink.getServiceLinkId(),
          mutatedServiceLink.getServiceType(),
          mutatedServiceLink.getLinkStatus());
    }
    // MOE:begin_strip
    // LAB(replace solution)
    // // TODO(acceptLink): Using the mutateServiceLinks method in CustomerService, accept the
    // // proposed link between the new AdWords account and the new Merchant Center account.
    // LAB(end solution)
    // MOE:end_strip
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
