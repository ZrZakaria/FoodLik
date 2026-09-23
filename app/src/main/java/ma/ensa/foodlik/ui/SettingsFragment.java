package ma.ensa.foodlik.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import ma.ensa.foodlik.LoginActivity;
import ma.ensa.foodlik.R;
import ma.ensa.foodlik.data.PreferencesManager;

public class SettingsFragment extends Fragment {
    private PreferencesManager prefs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        prefs = new PreferencesManager(requireContext());
        
        TextView profileName = view.findViewById(R.id.profileName);
        TextView profileEmail = view.findViewById(R.id.profileEmail);
        TextView profileInitial = view.findViewById(R.id.profileInitial);
        
        if (prefs.isGuest()) {
            profileName.setText("Guest User");
            profileEmail.setText("guest@foodlik.ma");
            profileInitial.setText("G");
        } else {
            String email = prefs.getUserEmail();
            profileName.setText(email.split("@")[0]);
            profileEmail.setText(email);
            profileInitial.setText(email.substring(0, 1).toUpperCase());
        }

        EditText phone = view.findViewById(R.id.customerPhoneInput);
        EditText address = view.findViewById(R.id.deliveryAddressInput);
        Button save = view.findViewById(R.id.saveSettingsButton);
        Button logout = view.findViewById(R.id.logoutButton);

        phone.setText(prefs.getCustomerPhone());
        address.setText(prefs.getDeliveryAddress());

        save.setOnClickListener(v -> {
            prefs.setCustomerPhone(phone.getText().toString().trim());
            prefs.setDeliveryAddress(address.getText().toString().trim());
            Toast.makeText(requireContext(), "Saved", Toast.LENGTH_SHORT).show();
        });

        logout.setOnClickListener(v -> {
            prefs.logout();
            Intent intent = new Intent(requireActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }
}
