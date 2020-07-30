package com.xuezhy.mapdrawdemo.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import com.xuezhy.mapdrawdemo.R;


/**
 * 类功能描述:
 * 作者:        zhongyangxue
 * 创建时间:     2020-01-12 17:05
 * 邮箱         1366411749@qq.com
 * 版本:        1.0
 */
public class PopWindowUtil {

    public static final AlertDialog buildNoTitleEnsureDialog(Context context, String content, String cancel, String sure, final EnsureListener listener) {
        if (context instanceof Activity) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                if (((Activity) context).isDestroyed())
                    return null;
            }
        }

        final AlertDialog dialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(false);
        dialog = builder.create();

        dialog.show();
        View baseView = LayoutInflater.from(context).inflate(R.layout.layout_ensure_notitle_dialog, null);
        ((TextView) baseView.findViewById(R.id.dialog_cancel_btn)).setText(cancel);
        ((TextView) baseView.findViewById(R.id.dialog_sure_btn)).setText(sure);
        if (!TextUtils.isEmpty(content)) {
            ((TextView) baseView.findViewById(R.id.dialog_content)).setText(content);
        } else {
            baseView.findViewById(R.id.dialog_content).setVisibility(View.GONE);
        }
        baseView.findViewById(R.id.dialog_sure_btn).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (listener != null)
                    listener.sure(null);
                dialog.dismiss();
            }
        });

        baseView.findViewById(R.id.dialog_cancel_btn).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (listener != null)
                    listener.cancel();
                dialog.dismiss();
            }
        });
        WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
        lp.width = ScreenUtil.getPxByDp(context,320);//定义宽度
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;//定义高度
        dialog.getWindow().setAttributes(lp);
        dialog.setContentView(baseView);
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        return dialog;
    }

    public interface EnsureListener {
        void sure(Object obj);

        void cancel();
    }


}
