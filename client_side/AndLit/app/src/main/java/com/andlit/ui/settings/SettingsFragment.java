package com.andlit.ui.settings;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import com.andlit.R;
import com.andlit.cron.CronMaster;
import com.andlit.cron.jobs.BackupJob;
import com.andlit.cron.jobs.TrainingJob;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.widget.Toast;
import static com.andlit.ui.settings.SettingsDefinedKeys.BACKUP_FREQUENCY;
import static com.andlit.ui.settings.SettingsDefinedKeys.LANGUAGE;
import static com.andlit.ui.settings.SettingsDefinedKeys.TRAINING_FREQUENCY;

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener
{
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey)
    {
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }

    // Action Listeners for when settings change
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
        if( key.equals(TRAINING_FREQUENCY) )
        {
            // Do something when training frequency is changed
            Context context = this.getActivity().getApplicationContext();
            CharSequence text = "Training Frequency Changed!";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            CronMaster.rescheduleJob(this.getContext(), TrainingJob.TAG,true);
        }

        if( key.equals(BACKUP_FREQUENCY) )
        {
            // Do something when backup frequency is changed
            Context context = this.getActivity().getApplicationContext();
            CharSequence text = "Backup Frequency Changed!";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, duration);
            CronMaster.rescheduleJob(this.getContext(), BackupJob.TAG,true);
            toast.show();
        }

        if( key.equals(LANGUAGE) )
        {
            // Do something when language is changed
            Context context = this.getActivity().getApplicationContext();
            CharSequence text = "Language Changed!";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }
}
