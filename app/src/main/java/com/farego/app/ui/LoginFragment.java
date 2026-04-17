package com.farego.app.ui;
// ============================================================
// FILE: app/src/main/java/com/farego/app/ui/LoginFragment.java
// PURPOSE: Login screen — validates credentials via AuthViewModel,
//          saves session on success, navigates to MainActivity.
// ============================================================

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.farego.app.R;
import com.farego.app.databinding.FragmentLoginBinding;
import com.farego.app.utils.SessionManager;
import com.farego.app.viewmodel.AuthViewModel;
import com.google.android.material.snackbar.Snackbar;

public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;
    private AuthViewModel        authVm;
    private SessionManager       session;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        authVm  = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);
        session = new SessionManager(requireContext());

        // ── Observers ─────────────────────────────────────────
        authVm.loggedInUser.observe(getViewLifecycleOwner(), user -> {
            if (user == null) return;
            // Save session to SharedPreferences
            session.saveSession(user.id, user.username, user.isAdmin);
            // Navigate to main app
            startActivity(new Intent(requireContext(), MainActivity.class));
            requireActivity().finish();
        });

        authVm.errorMsg.observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) {
                Snackbar.make(view, msg, Snackbar.LENGTH_LONG).show();
                authVm.errorMsg.setValue(null);
            }
        });

        authVm.isLoading.observe(getViewLifecycleOwner(), loading -> {
            binding.btnLogin.setEnabled(!loading);
            binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        });

        // ── Click handlers ────────────────────────────────────
        binding.btnLogin.setOnClickListener(v -> {
            String username = binding.etUsername.getText().toString().trim();
            String password = binding.etPassword.getText().toString().trim();
            authVm.login(username, password);
        });

        binding.tvRegister.setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.action_login_to_register));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}