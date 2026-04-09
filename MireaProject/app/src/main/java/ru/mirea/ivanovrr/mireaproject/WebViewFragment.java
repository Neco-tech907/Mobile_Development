package ru.mirea.ivanovrr.mireaproject;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.KeyEvent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceError;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import ru.mirea.ivanovrr.mireaproject.databinding.FragmentWebViewBinding;

public class WebViewFragment extends Fragment {

    private static final String WEB_VIEW_STATE_KEY = "web_view_state";

    private FragmentWebViewBinding binding;
    private Bundle webViewState;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentWebViewBinding.inflate(inflater, container, false);
        if (savedInstanceState != null) {
            webViewState = savedInstanceState.getBundle(WEB_VIEW_STATE_KEY);
        }
        setupControls();
        configureWebView();
        if (webViewState != null) {
            binding.webView.restoreState(webViewState);
            updateNavigationState();
        } else {
            openPage(getString(R.string.web_default_url));
        }
        return binding.getRoot();
    }

    private void setupControls() {
        binding.backButton.setOnClickListener(view -> navigateBack());
        binding.openButton.setOnClickListener(view -> {
            String rawUrl = binding.addressEditText.getText() == null
                    ? ""
                    : binding.addressEditText.getText().toString().trim();
            openPage(rawUrl);
        });
        binding.addressEditText.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            boolean isEnterPressed = keyEvent != null
                    && keyEvent.getAction() == KeyEvent.ACTION_DOWN
                    && keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER;
            if (!isEnterPressed) {
                return false;
            }
            String rawUrl = textView.getText() == null ? "" : textView.getText().toString().trim();
            openPage(rawUrl);
            return true;
        });
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void configureWebView() {
        WebSettings settings = binding.webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);

        binding.webView.setWebChromeClient(new WebChromeClient());
        binding.webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return false;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                setLoadingState(true);
                binding.addressEditText.setText(url);
                updateNavigationState();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                setLoadingState(false);
                binding.addressEditText.setText(url);
                updateNavigationState();
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                if (request.isForMainFrame()) {
                    setLoadingState(false);
                    binding.addressInputLayout.setError(getString(R.string.web_error_loading));
                }
            }
        });
    }

    private void openPage(String rawUrl) {
        String normalizedUrl = normalizeUrl(rawUrl);
        if (normalizedUrl == null) {
            binding.addressInputLayout.setError(getString(R.string.web_error_invalid_url));
            return;
        }
        binding.addressInputLayout.setError(null);
        binding.webView.loadUrl(normalizedUrl);
        binding.addressEditText.setText(normalizedUrl);
    }

    private String normalizeUrl(String rawUrl) {
        if (TextUtils.isEmpty(rawUrl)) {
            return getString(R.string.web_default_url);
        }
        if (rawUrl.contains(" ")) {
            return null;
        }
        if (rawUrl.startsWith("http://") || rawUrl.startsWith("https://")) {
            return rawUrl;
        }
        return "https://" + rawUrl;
    }

    private void setLoadingState(boolean isLoading) {
        binding.webProgress.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.statusText.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }

    private void navigateBack() {
        if (binding != null && binding.webView.canGoBack()) {
            binding.webView.goBack();
            updateNavigationState();
        }
    }

    private void updateNavigationState() {
        binding.backButton.setEnabled(binding.webView.canGoBack());
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (binding != null) {
            Bundle state = new Bundle();
            binding.webView.saveState(state);
            outState.putBundle(WEB_VIEW_STATE_KEY, state);
        }
    }

    @Override
    public void onDestroyView() {
        if (binding != null) {
            binding.webView.stopLoading();
            binding.webView.destroy();
            binding = null;
        }
        super.onDestroyView();
    }
}
