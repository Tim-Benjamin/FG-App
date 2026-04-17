package com.farego.app.ui;
// ============================================================
// FILE: app/src/main/java/com/farego/app/ui/UserDashboardFragment.java
// PURPOSE: Shows user's past fare searches in a RecyclerView,
//          sorted by timestamp DESC. Includes logout menu item.
// ============================================================

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.farego.app.R;
import com.farego.app.databinding.FragmentUserDashboardBinding;
import com.farego.app.ui.adapter.HistoryAdapter;
import com.farego.app.utils.SessionManager;
import com.farego.app.viewmodel.HistoryViewModel;

public class UserDashboardFragment extends Fragment {

    private FragmentUserDashboardBinding binding;
    private HistoryViewModel             historyVm;
    private HistoryAdapter               adapter;
    private SessionManager               session;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentUserDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        session   = new SessionManager(requireContext());
        historyVm = new ViewModelProvider(this).get(HistoryViewModel.class);

        // ── Greeting ───────────────────────────────────────────
        binding.tvGreeting.setText("Welcome, " + session.getUsername());

        // ── RecyclerView ───────────────────────────────────────
        adapter = new HistoryAdapter();
        binding.rvHistory.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvHistory.setAdapter(adapter);

        historyVm.getHistory(session.getUserId()).observe(getViewLifecycleOwner(), history -> {
            if (history == null || history.isEmpty()) {
                binding.tvEmpty.setVisibility(View.VISIBLE);
                binding.rvHistory.setVisibility(View.GONE);
            } else {
                binding.tvEmpty.setVisibility(View.GONE);
                binding.rvHistory.setVisibility(View.VISIBLE);
                adapter.submitList(history);
            }
        });

        binding.btnBack.setOnClickListener(v ->
                requireActivity().getOnBackPressedDispatcher().onBackPressed());
    }

    // ── Options menu (logout) ─────────────────────────────────
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_dashboard, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            session.clearSession();
            startActivity(new Intent(requireContext(), AuthActivity.class));
            requireActivity().finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyView() { super.onDestroyView(); binding = null; }
}