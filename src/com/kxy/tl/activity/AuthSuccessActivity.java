package com.kxy.tl.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;

import com.kxy.tl.R;
import com.vunke.tl.auth.Auth;
import com.vunke.tl.base.BaseActivity;
import com.vunke.tl.base.UIUtil;
import com.vunke.tl.bean.GroupStrategy.GroupStrategyBean;
import com.vunke.tl.util.Constants;
import com.vunke.tl.util.LogUtil;
import com.vunke.tl.util.SharedPreferencesUtil;

import java.util.Date;

public class AuthSuccessActivity extends BaseActivity{
	private Intent intent;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_auth_success);
		LogUtil.i("tv_launcher", "AuthSuccessActivity: onCreate ");
		intent = getIntent();
	}
	@Override
	protected void onResume() {
		LogUtil.i("tv_launcher", "AuthSuccessActivity: onResume ");
		super.onResume();
		if (intent == null) {
			intent = getIntent();
		}
		if (intent !=null) {
			if (intent.hasExtra("user_id")) {
				startEPG(mcontext, intent.getStringExtra("user_id"));
			}
		}
	}
	public void startEPG(Context context,String user_id){
		LogUtil.i("tv_launcher", "get AuthCode :AUTH_CODE_AUTH_SUCCESS");
		if (TextUtils.isEmpty(user_id)) {
			LogUtil.e("tv_launcher", "get user_id is null ,start epg error");
			UIUtil.sendBroadCast(context, Constants.ADVERTISING_ACTION,
					new Intent());// 节目播放业务
			SharedPreferencesUtil.setBooleanValue(context,
					SharedPreferencesUtil.IS_PALYED_ADVERT, true);
			// finish();
			LogUtil.i("tv_launcher",
					"send BroadCast to play iptv,start time:" + new Date());
			return;
		}
		String[] strings = new String[] { user_id.trim() };
		Uri localUri = Uri
				.parse("content://com.vunke.tvlauncher.provider2/group_strategy");
		Cursor localCursor = context.getContentResolver().query(localUri,
				null, null, strings, null);
		GroupStrategyBean bean = new GroupStrategyBean();
		try {
			if (localCursor.moveToNext()) {
				bean.setEPGcode(localCursor.getString(localCursor
						.getColumnIndex("epg_code")));
				bean.setEPGpackage(localCursor.getString(localCursor
						.getColumnIndex("epg_package")));
				bean.setGroupAddress(localCursor.getString(localCursor
						.getColumnIndex("group_address")));
				bean.setGroupName(localCursor.getString(localCursor
						.getColumnIndex("group_name")));
				bean.setGroupStatus(localCursor.getString(localCursor
						.getColumnIndex("group_status")));
				bean.setGroupType(localCursor.getString(localCursor
						.getColumnIndex("group_type")));
				bean.setGrpupNumber(localCursor.getString(localCursor
						.getColumnIndex("group_number")));
				// bean.setCreateTime(localCursor.getString(localCursor.getColumnIndex("create_time")));
				bean.setUserId(localCursor.getString(localCursor
						.getColumnIndex("user_id")));
			}
		} catch (Exception e){
			LogUtil.e("tv_launcher","get group_strategy error ,sql select failed");
			bean.setUserId(user_id.trim());
			UIUtil.StartLastEpg(context, bean);
		}finally {
			if (localCursor != null)
				localCursor.close();
		}
		if (!TextUtils.isEmpty(bean.getEPGpackage())) {
			// LogUtil.e("tv_launcher", "bean :"+ bean.toString());
			PackageInfo getPackageInfo = Auth.GetPackageInfo(context,
					bean.getEPGpackage());
			if (getPackageInfo != null) {
				LogUtil.e(
						"tv_launcher",
						"get epg_package info success ,start epg :"
								+ bean.getEPGpackage());
				UIUtil.StartEPG(bean.getEPGpackage(), context);
				UIUtil.setPackageName(context, bean.getUserId(),
						bean.getEPGpackage());
			} else {
				UIUtil.StartLastEpg(context, bean);
			}
		} else {
			UIUtil.StartLastEpg(context, bean);
		}
	
	}
//	@Override
//	public boolean onKeyDown(int keyCode, KeyEvent event) {
//		if (keyCode == KeyEvent.KEYCODE_BACK) {
//			return false;
//		}
//		return super.onKeyDown(keyCode, event);
//	}
}
