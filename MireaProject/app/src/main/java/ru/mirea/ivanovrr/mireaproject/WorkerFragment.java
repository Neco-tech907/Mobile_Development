package ru.mirea.ivanovrr.mireaproject;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import ru.mirea.ivanovrr.mireaproject.databinding.FragmentWorkerBinding;

public class WorkerFragment extends Fragment {
    private FragmentWorkerBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentWorkerBinding.inflate(inflater, container, false);

        binding.btnStartWorker.setOnClickListener(v -> {
            OneTimeWorkRequest workRequest =
                    new OneTimeWorkRequest.Builder(Worker.class).build();
            WorkManager.getInstance(requireContext()).enqueue(workRequest);
            Toast.makeText(requireContext(), R.string.worker_task_enqueued, Toast.LENGTH_SHORT).show();
        });

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
