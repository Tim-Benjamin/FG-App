package com.farego.app.ui;
// ============================================================
// FILE: app/src/main/java/com/farego/app/ui/RegisterFragment.java
// PURPOSE: Registration screen. Creates new user in Room,
//          auto-logs in on success.
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
import com.farego.app.databinding.FragmentRegisterBinding;
import com.farego.app.utils.SessionManager;
import com.farego.app.viewmodel.AuthViewModel;
import com.google.android.material.snackbar.Snackbar;

public class RegisterFragment extends Fragment {

    private FragmentRegisterBinding binding;
    private AuthViewModel           authVm;
    private SessionManager          session;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        authVm  = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);
        session = new SessionManager(requireContext());

        authVm.loggedInUser.observe(getViewLifecycleOwner(), user -> {
            if (user == null) return;
            session.saveSession(user.id, user.username, user.isAdmin);
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
            binding.btnRegister.setEnabled(!loading);
            binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        });

        binding.btnRegister.setOnClickListener(v -> authVm.register(
                binding.etUsername.getText().toString().trim(),
                binding.etPassword.getText().toString().trim(),
                binding.etConfirmPassword.getText().toString().trim(),
                binding.etEmail.getText().toString().trim()
        ));

        binding.tvLogin.setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.action_register_to_login));
    }

    @Override public void onDestroyView() { super.onDestroyView(); binding = null; }
}