package eu.veldsoft.share.with.me;

import java.util.UUID;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings.Secure;
import android.view.View;
import android.widget.Button;

/**
 * 
 * @author
 */
public class LobbyActivity extends Activity {

	/**
	 * 
	 */
	private void registerHash() {
		String hash = Secure.getString(this.getContentResolver(), Secure.ANDROID_ID);
		if (hash == null || hash == "") {
			hash = Long.toHexString(UUID.randomUUID().getLeastSignificantBits());
		}

		SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(this);
		String stored = preference.getString(Util.INSTNCE_HASH_CODE_KEY, "");

		/*
		 * Write only once in the first start of the application.
		 */
		if (stored.equals("") == true) {
			SharedPreferences.Editor editor = preference.edit();
			editor.putString(Util.INSTNCE_HASH_CODE_KEY, hash);
			editor.commit();
		}
	}

	/**
	 * 
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lobby);

		/*
		 * Check every time on application load.
		 */
		registerHash();

		((Button) findViewById(R.id.request)).setOnClickListener(new View.OnClickListener() {
			/**
			 * 
			 */
			@Override
			public void onClick(View view) {
				startActivity(new Intent(LobbyActivity.this, RequestActivity.class));
			}
		});

		((Button) findViewById(R.id.team)).setOnClickListener(new View.OnClickListener() {
			/**
			 * 
			 */
			@Override
			public void onClick(View view) {
				startActivity(new Intent(LobbyActivity.this, JoinTeamActivity.class));
			}
		});

		((Button) findViewById(R.id.about)).setOnClickListener(new View.OnClickListener() {
			/**
			 * 
			 */
			@Override
			public void onClick(View view) {
				startActivity(new Intent(LobbyActivity.this, AboutActivity.class));
			}
		});

		((Button) findViewById(R.id.exit)).setOnClickListener(new View.OnClickListener() {
			/**
			 * 
			 */
			@Override
			public void onClick(View view) {
				LobbyActivity.this.finish();
			}
		});

	}
}
