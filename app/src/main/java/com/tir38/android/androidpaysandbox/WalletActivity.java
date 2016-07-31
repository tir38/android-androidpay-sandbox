package com.tir38.android.androidpaysandbox;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import com.google.android.gms.identity.intents.model.CountrySpecification;
import com.google.android.gms.wallet.Cart;
import com.google.android.gms.wallet.MaskedWalletRequest;
import com.google.android.gms.wallet.PaymentMethodTokenizationParameters;
import com.google.android.gms.wallet.PaymentMethodTokenizationType;
import com.google.android.gms.wallet.WalletConstants;
import com.google.android.gms.wallet.fragment.SupportWalletFragment;
import com.google.android.gms.wallet.fragment.WalletFragmentInitParams;
import com.google.android.gms.wallet.fragment.WalletFragmentMode;
import com.google.android.gms.wallet.fragment.WalletFragmentOptions;

public class WalletActivity extends SingleFragmentActivity {

    private static final int MASKED_WALLET_REQUEST_CODE = 100;
    private static final String CURRENCY_CODE = "USD"; // ISO 4217 currency code
    private static final String COUNTRY_CODE = "US"; // ISO 3166-2 country code
    private static final String PUBLIC_KEY = "BO39Rh43UGXMQy5PAWWe7UGWd2a9YRjNLPEEVe+zWIbdIgALcDcnYCuHbmrrzl7h8FZjl6RCzoi5/cDrqXNRV"; // stolen from sample app

    public static Intent newIntent(Context context) {
        return new Intent(context, WalletActivity.class);
    }

    @Override
    protected Fragment createFragment() {

        // TODO think about what is the correct UX for what might happen if any of this fails
        // TODO confirm that this setup is all synchronous (and not slow)

        // TODO optional: setup Wallet Style

        WalletFragmentOptions walletFragmentOptions = WalletFragmentOptions.newBuilder()
                .setEnvironment(WalletConstants.ENVIRONMENT_TEST) // environment_test is default
                // TODO optional: .setFragmentStyle()
                .setMode(WalletFragmentMode.BUY_BUTTON) // buy_button is default
                .setTheme(WalletConstants.THEME_DARK) // default is theme_dark
                .build();

        SupportWalletFragment supportWalletFragment = SupportWalletFragment.newInstance(walletFragmentOptions);

        MaskedWalletRequest maskedWalletRequest = createMaskedWalletRequest();

        WalletFragmentInitParams initParams = createWalletFragmentInitParams(maskedWalletRequest);

        supportWalletFragment.initialize(initParams);

        return supportWalletFragment;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case MASKED_WALLET_REQUEST_CODE:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        // TODO get MaskedWallet from Intent and launch confirmation page
                        break;
                    case Activity.RESULT_CANCELED:
                        // TODO user canceled or hit back button
                        break;
                    default:
                        if (data != null) {
                            int errorCode = data.getIntExtra(WalletConstants.EXTRA_ERROR_CODE, -1);
                            handleError(errorCode);
                        }
                        break;
                }
                break;

            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private PaymentMethodTokenizationParameters createTokenParams() {
        // PaymentMethodTokenizationParameters is Tokenization parameters
        // passed by the integrator used to tokenize the credit card selected by the user.
        // https://developers.google.com/android/reference/com/google/android/gms/wallet/PaymentMethodTokenizationParameters

        return PaymentMethodTokenizationParameters.newBuilder()
                .setPaymentMethodTokenizationType(PaymentMethodTokenizationType.NETWORK_TOKEN)
                .addParameter("publicKey", PUBLIC_KEY)
                .build();
    }

    private MaskedWalletRequest createMaskedWalletRequest() {
        PaymentMethodTokenizationParameters tokenizationParameters = createTokenParams();

        return MaskedWalletRequest.newBuilder()

                // required fields
                .setCurrencyCode(CURRENCY_CODE)
                .setEstimatedTotalPrice("10.00") // format follows the regex: [0-9]+(\.[0-9][0-9])?.

                // optional fields
                .setShippingAddressRequired(false)
                .setMerchantName("merchant name")
                .setPhoneNumberRequired(false)
                .setPaymentMethodTokenizationParameters(tokenizationParameters)
                .setAllowDebitCard(true)
                .setAllowPrepaidCard(false)
                .setMerchantTransactionId("1234")
                .addAllowedCardNetwork(123) // INTERESTING: if you pass in wrong value here, onActivityResult will receive error code = 8
                .addAllowedCountrySpecificationForShipping(new CountrySpecification(COUNTRY_CODE))

                // Sets an optional shopping cart to use for this purchase.
                // Supplying as much information about your transaction in
                // the cart can help improve the user experience during the
                // payment flow. If you add a shipping or tax line item to
                // this cart, make sure to use a description that informs
                // the user that the line items are estimates (such as
                // "Estimated Shipping").
                .setCart(Cart.newBuilder()
                        .setCurrencyCode(CURRENCY_CODE)
                        // TODO .setLineItems(...)
                        .setTotalPrice("10.00") // format follows the regex: [0-9]+(\.[0-9][0-9])?.
                        .build())

                .build();
    }

    private WalletFragmentInitParams createWalletFragmentInitParams(MaskedWalletRequest maskedWalletRequest) {
        return WalletFragmentInitParams.newBuilder()
                .setAccountName("account name")
                // .setMaskedWallet(...) // user EITHER this OR setMaskedWalletRequest()
                .setMaskedWalletRequest(maskedWalletRequest)
                .setMaskedWalletRequestCode(MASKED_WALLET_REQUEST_CODE)
                .build();
    }

    private void handleError(int errorCode) {
        switch (errorCode) {
            case WalletConstants.ERROR_CODE_SPENDING_LIMIT_EXCEEDED:
                Toast.makeText(this, "ERROR_CODE_SPENDING_LIMIT_EXCEEDED", Toast.LENGTH_SHORT).show();
                break;

            case WalletConstants.ERROR_CODE_INVALID_PARAMETERS:
                Toast.makeText(this, "ERROR_CODE_INVALID_PARAMETERS", Toast.LENGTH_SHORT).show();
                break;

            case WalletConstants.ERROR_CODE_AUTHENTICATION_FAILURE:
                Toast.makeText(this, "ERROR_CODE_AUTHENTICATION_FAILURE", Toast.LENGTH_SHORT).show();
                break;

            case WalletConstants.ERROR_CODE_BUYER_ACCOUNT_ERROR:
                Toast.makeText(this, "ERROR_CODE_BUYER_ACCOUNT_ERROR", Toast.LENGTH_SHORT).show();
                break;

            case WalletConstants.ERROR_CODE_MERCHANT_ACCOUNT_ERROR:
                Toast.makeText(this, "ERROR_CODE_MERCHANT_ACCOUNT_ERROR", Toast.LENGTH_SHORT).show();
                break;

            case WalletConstants.ERROR_CODE_SERVICE_UNAVAILABLE:
                Toast.makeText(this, "ERROR_CODE_SERVICE_UNAVAILABLE", Toast.LENGTH_SHORT).show();
                break;

            case WalletConstants.ERROR_CODE_UNSUPPORTED_API_VERSION:
                Toast.makeText(this, "ERROR_CODE_UNSUPPORTED_API_VERSION", Toast.LENGTH_SHORT).show();
                break;

            case WalletConstants.ERROR_CODE_UNKNOWN:
                Toast.makeText(this, "ERROR_CODE_UNKNOWN", Toast.LENGTH_SHORT).show();
                break;

            default:
                // unrecoverable error
                Toast.makeText(this, "unknown error code: " + errorCode, Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
