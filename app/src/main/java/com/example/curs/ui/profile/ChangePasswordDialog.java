package com.example.curs.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.curs.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordDialog extends DialogFragment {

    private TextInputEditText etOld, etNew;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_change_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        etOld = view.findViewById(R.id.etOldPassword);
        etNew = view.findViewById(R.id.etNewPassword);
        MaterialButton btn = view.findViewById(R.id.btnChangePasswordConfirm);

        btn.setOnClickListener(v -> changePassword());
    }

    private void changePassword() {
        String oldPw = etOld.getText() != null ? etOld.getText().toString().trim() : "";
        String newPw = etNew.getText() != null ? etNew.getText().toString().trim() : "";

        if (oldPw.isEmpty() || newPw.isEmpty()) {
            Toast.makeText(getContext(), "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }
        if (newPw.length() < 6) {
            Toast.makeText(getContext(), "Пароль должен быть ≥ 6 символов", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || user.getEmail() == null) return;

        user.reauthenticate(EmailAuthProvider.getCredential(user.getEmail(), oldPw))
                .addOnSuccessListener(aVoid ->
                        user.updatePassword(newPw)
                                .addOnSuccessListener(aVoid2 -> {
                                    Toast.makeText(getContext(),
                                            "Пароль изменён", Toast.LENGTH_SHORT).show();
                                    dismiss();
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(getContext(),
                                                "Ошибка: " + e.getMessage(),
                                                Toast.LENGTH_LONG).show()))
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(),
                                "Старый пароль неверный", Toast.LENGTH_LONG).show());
    }
}