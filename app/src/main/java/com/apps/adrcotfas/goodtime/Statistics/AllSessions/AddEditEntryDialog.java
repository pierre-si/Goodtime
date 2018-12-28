package com.apps.adrcotfas.goodtime.Statistics.AllSessions;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import com.apps.adrcotfas.goodtime.LabelAndColor;
import com.apps.adrcotfas.goodtime.Main.LabelsViewModel;
import com.apps.adrcotfas.goodtime.R;
import com.apps.adrcotfas.goodtime.Session;
import com.apps.adrcotfas.goodtime.Statistics.Main.SelectLabelDialog;
import com.apps.adrcotfas.goodtime.Statistics.SessionViewModel;
import com.apps.adrcotfas.goodtime.Util.DatePickerFragment;
import com.apps.adrcotfas.goodtime.Util.TimePickerFragment;
import com.apps.adrcotfas.goodtime.Util.StringUtils;
import com.apps.adrcotfas.goodtime.databinding.DialogAddEntryBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.joda.time.DateTime;

import java.util.Calendar;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProviders;

import static com.apps.adrcotfas.goodtime.Statistics.AllSessions.AddEditEntryDialogViewModel.INVALID_SESSION_TO_EDIT_ID;

public class AddEditEntryDialog extends BottomSheetDialogFragment implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener, SelectLabelDialog.OnLabelSelectedListener {

    private AddEditEntryDialogViewModel mViewModel;
    private SessionViewModel mSessionViewModel;

    private Session mSessionToEdit;
    private LabelsViewModel mLabelsViewModel;

    public AddEditEntryDialog() {
        // Empty constructor required for DialogFragment
    }

    /**
     * Creates a new instance from an existing session. To be used when editing a session.
     * @param session the session
     * @return the new instance initialized with the existing session's data
     */
    public static AddEditEntryDialog newInstance(Session session) {
        AddEditEntryDialog dialog = new AddEditEntryDialog();
        dialog.mSessionToEdit = session;
        return dialog;
    }

    @SuppressLint("ResourceType")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        DialogAddEntryBinding binding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.dialog_add_entry, null, false);

        View view = binding.getRoot();
        mViewModel = ViewModelProviders.of(this).get(AddEditEntryDialogViewModel.class);
        mSessionViewModel = ViewModelProviders.of(this).get(SessionViewModel.class);
        mLabelsViewModel = ViewModelProviders.of(this).get(LabelsViewModel.class);

        // TODO: implement onClicks with DataBinding
        mViewModel.duration.observe(this, d -> {
            String duration = d.toString();
            binding.duration.setText(duration);
            binding.duration.setSelection(duration.length());
        });
        mViewModel.date.observe(this, date ->  {
            binding.editDate.setText(StringUtils.formatDate(date.getMillis()));
            binding.editTime.setText(StringUtils.formatTime(date.getMillis()));
        });

        mViewModel.label.observe(this, label -> {
            if (label != null) {
                binding.labelChip.setText(label);
                mLabelsViewModel.getColorOfLabel(label).observe(this, color ->
                        binding.labelChip.setChipBackgroundColor(ColorStateList.valueOf(color)));
                binding.labelDrawable.setImageDrawable(getResources().getDrawable(R.drawable.ic_label));
            } else {
                binding.labelChip.setText("add label");
                binding.labelChip.setChipBackgroundColor(ColorStateList.valueOf(getResources().getColor(R.color.white)));
                binding.labelDrawable.setImageDrawable(getResources().getDrawable(R.drawable.ic_label_off));
            }
        });

        binding.editDate.setOnClickListener(v -> onDateViewClick());
        binding.editTime.setOnClickListener(v -> onTimeViewClick());

        binding.labelChip.setOnClickListener(c -> {
            // open another dialog to select the chip
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            SelectLabelDialog.newInstance(this, mViewModel.label.getValue(), false).show(fragmentManager, "");
        });

        binding.save.setOnClickListener(v -> {
            if (binding.duration.getText().toString().isEmpty()) {
                Toast.makeText(getActivity(), "Enter a valid duration", Toast.LENGTH_LONG).show();
            }
            else {
                final int duration = Math.min(Integer.parseInt(binding.duration.getText().toString()), 120);
                final String label = mViewModel.label.getValue();
                Session sessionToAdd = new Session(0, mViewModel.date.getValue().getMillis(), duration, label);
                if (mViewModel.sessionToEditId != INVALID_SESSION_TO_EDIT_ID) {
                    mSessionViewModel.editSession(mViewModel.sessionToEditId, sessionToAdd.endTime, sessionToAdd.totalTime, sessionToAdd.label);
                } else {
                    mSessionViewModel.addSession(sessionToAdd);
                }
                dismiss();
            }
        });

        // this is for the edit dialog
        if (mSessionToEdit != null) {
            mViewModel.date.setValue(new DateTime(mSessionToEdit.endTime));
            mViewModel.duration.setValue(mSessionToEdit.totalTime);
            mViewModel.label.setValue(mSessionToEdit.label);
            mViewModel.sessionToEditId = mSessionToEdit.id;
            binding.header.setText("Edit session");
        }

        return view;
    }

    private Calendar getCalendar() {
        Calendar calendar = Calendar.getInstance();
        DateTime date = mViewModel.date.getValue();
        if (date != null) {
            calendar.setTime(date.toDate());
        }
        return calendar;
    }

    private void onTimeViewClick() {
        TimePickerFragment d = TimePickerFragment.newInstance(AddEditEntryDialog.this, getCalendar());
        FragmentManager fragmentManager = getFragmentManager();
        if (fragmentManager != null) {
            d.show(fragmentManager, "");
        }
    }

    private void onDateViewClick() {
        DialogFragment d = DatePickerFragment.newInstance(AddEditEntryDialog.this, getCalendar());
        FragmentManager fragmentManager = getFragmentManager();
        if (fragmentManager != null) {
            d.show(fragmentManager, "");
        }
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        mViewModel.date.setValue((mViewModel.date.getValue() == null)
                ? new DateTime()
                : mViewModel.date.getValue()
                .withHourOfDay(hourOfDay)
                .withMinuteOfHour(minute));
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        mViewModel.date.setValue((mViewModel.date.getValue() == null)
                ? new DateTime()
                : mViewModel.date.getValue()
                .withYear(year)
                .withMonthOfYear(monthOfYear + 1)
                .withDayOfMonth(dayOfMonth));
    }

    @Override
    public void onLabelSelected(LabelAndColor labelAndColor) {
        if (labelAndColor != null) {
            mViewModel.label.setValue(labelAndColor.label);
        } else {
            mViewModel.label.setValue(null);
        }
    }
}
