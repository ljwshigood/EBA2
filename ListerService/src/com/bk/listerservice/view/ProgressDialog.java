package com.bk.listerservice.view;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;

import com.bk.listerservice.R;

public class ProgressDialog extends Dialog{

	private Context mContext ;
	
	private String content ;
	
	private TextView mTvContent ;
	
	private void initView(){
		mTvContent = (TextView)findViewById(R.id.tv_content);
		mTvContent.setText(content);
	}
	
	public ProgressDialog(Context context) {
		super(context);
	}

	public ProgressDialog(Context context, boolean cancelable,
			OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
	}

	public ProgressDialog(Context context, int theme,String content) {
		super(context, theme);
		this.mContext = context ;
		this.content = content ;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		 this.setContentView(R.layout.progress_dialog);
		 initView();
	}
	
}
