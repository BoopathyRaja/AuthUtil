package com.br.authutil.dialog;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.br.authutil.R;
import com.br.commonutils.provider.DialogNegativeCallback;
import com.br.commonutils.util.CommonUtil;
import com.br.commonutils.validator.Validator;
import com.br.commonutils.view.textview.TextView;

public class FingerprintDialog extends BottomSheetDialog implements View.OnClickListener {

    private Context context;
    private int icon;
    private String title;
    private String subTitle;
    private String description;
    private String buttonText;
    private DialogNegativeCallback dialogNegativeCallback;

    private View view;

    public FingerprintDialog(@NonNull Context context) {
        this(context, R.style.BottomSheetDialog);
    }

    public FingerprintDialog(@NonNull Context context, int theme) {
        super(context, theme);

        this.context = context.getApplicationContext();
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(@DrawableRes int icon) {
        this.icon = icon;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getButtonText() {
        return buttonText;
    }

    public void setButtonText(String buttonText) {
        this.buttonText = buttonText;
    }

    public DialogNegativeCallback getDialogNegativeCallback() {
        return dialogNegativeCallback;
    }

    public void setDialogNegativeCallback(DialogNegativeCallback dialogNegativeCallback) {
        this.dialogNegativeCallback = dialogNegativeCallback;
    }

    public void show() {
        view = getLayoutInflater().inflate(R.layout.view_bottomsheet_fingerprint, null);
        setContentView(view);

        ImageView imageViewIcon = findViewById(R.id.bottomSheetFingerprint_imageView_icon);
        imageViewIcon.setImageResource(icon);

        TextView textViewTitle = findViewById(R.id.bottomSheetFingerprint_textView_title);
        textViewTitle.setText(title);

        TextView textViewSubTitle = findViewById(R.id.bottomSheetFingerprint_textView_subTitle);
        textViewSubTitle.setText(subTitle);

        TextView textViewDescription = findViewById(R.id.bottomSheetFingerprint_textView_description);
        textViewDescription.setVisibility(Validator.isValid(description) ? View.VISIBLE : View.GONE);
        textViewDescription.setText(description);

        Button buttonCancel = findViewById(R.id.bottomSheetFingerprint_button_cancel);
        buttonCancel.setVisibility(Validator.isValid(buttonText) ? View.VISIBLE : View.GONE);
        buttonCancel.setOnClickListener(this::onClick);
        buttonCancel.setText(buttonText);

        super.show();
    }

    public void invalidate() {
        TextView textViewDescription = findViewById(R.id.bottomSheetFingerprint_textView_description);
        textViewDescription.setVisibility(Validator.isValid(description) ? View.VISIBLE : View.GONE);
        textViewDescription.setText(description);

        view.invalidate();
    }

    public void invalidateAndDismiss() {
        invalidate();
        CommonUtil.makeDelay(() -> dismiss());
    }

    public void dismiss() {
        if (isShowing())
            super.dismiss();
    }

    @Override
    public void setCancelable(boolean cancelable) {
        super.setCancelable(cancelable);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        if (id == R.id.bottomSheetFingerprint_button_cancel) {
            if (Validator.isValid(dialogNegativeCallback)) {
                dialogNegativeCallback.onNegativeClicked();
                return;
            }

            dismiss();
        }
    }

    public static class Builder {
        private Context context;
        private int icon;
        private String title;
        private String subTitle;
        private String description;
        private String buttonText;
        private DialogNegativeCallback dialogNegativeCallback;

        public Builder(@NonNull Context context) {
            this.context = context;
        }

        public Builder icon(@DrawableRes int icon) {
            this.icon = icon;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder subTitle(String subTitle) {
            this.subTitle = subTitle;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder buttonText(String buttonText) {
            this.buttonText = buttonText;
            return this;
        }

        public Builder dialogNegativeCallback(DialogNegativeCallback dialogNegativeCallback) {
            this.dialogNegativeCallback = dialogNegativeCallback;
            return this;
        }

        public FingerprintDialog build() {
            FingerprintDialog retVal = new FingerprintDialog(context);
            retVal.setIcon(icon);
            retVal.setTitle(title);
            retVal.setSubTitle(subTitle);
            retVal.setDescription(description);
            retVal.setButtonText(buttonText);
            retVal.setDialogNegativeCallback(dialogNegativeCallback);

            return retVal;
        }
    }
}
