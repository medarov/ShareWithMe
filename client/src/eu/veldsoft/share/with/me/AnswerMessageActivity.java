package eu.veldsoft.share.with.me;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import eu.veldsoft.share.with.me.model.Util;
import eu.veldsoft.share.with.me.storage.MessageHistoryDatabaseHelper;

/**
 * Message replay screen.
 * 
 * @author Ventsislav Medarov
 */
public class AnswerMessageActivity extends Activity {
	/**
	 * Database helper reference.
	 */
	MessageHistoryDatabaseHelper helper = null;

	/**
	 * Parent message hash code.
	 */
	private String parentHash = "";

	/**
	 * Message registration data-time.
	 */
	private String registered = "";

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_answer_message);

		/*
		 * If there is an answer screen already opened do not open a new one.
		 */
		// TODO This activity should not start more than once
		// simultaneously.
		/*
		 * if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT)
		 * != 0) { finish(); return; }
		 */

		helper = new MessageHistoryDatabaseHelper(AnswerMessageActivity.this);

		/*
		 * Obtain parent message hash code and registration time stamp.
		 */
		registered = (getIntent().getExtras() != null) ? getIntent()
				.getExtras().getString(Util.REGISTERED_KEY) : "";
		parentHash = (getIntent().getExtras() != null) ? getIntent()
				.getExtras().getString(Util.PARENT_MESSAGE_HASH_KEY) : "";

		/*
		 * Only mark the message as read.
		 */
		findViewById(R.id.replay_reject).setOnClickListener(
		/**
		 * {@inheritDoc}
		 */
		new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (helper == null) {
					return;
				}

				helper.setLastMessage(parentHash, registered);

				Toast.makeText(AnswerMessageActivity.this,
						R.string.discard_replay, Toast.LENGTH_SHORT).show();

				AnswerMessageActivity.this.finish();
			}
		});

		/*
		 * For replay later it is enough to close the activity.
		 */
		findViewById(R.id.replay_later).setOnClickListener(
		/**
		 * {@inheritDoc}
		 */
		new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				AnswerMessageActivity.this.finish();
			}
		});

		/*
		 * Send replay.
		 */
		findViewById(R.id.send_replay).setOnClickListener(
		/**
		 * {@inheritDoc}
		 */
		new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (helper == null) {
					return;
				}

				helper.setLastMessage(parentHash, registered);

				(new AsyncTask<Void, Void, Void>() {
					@Override
					protected Void doInBackground(Void... params) {
						String host = "";
						try {
							host = getPackageManager()
									.getApplicationInfo(
											AnswerMessageActivity.this
													.getPackageName(),
											PackageManager.GET_META_DATA).metaData
									.getString("host");
						} catch (NameNotFoundException exception) {
							System.err.println(exception);
							return null;
						}

						String script = "";
						try {
							script = getPackageManager().getActivityInfo(
									AnswerMessageActivity.this
											.getComponentName(),
									PackageManager.GET_ACTIVITIES
											| PackageManager.GET_META_DATA).metaData
									.getString("script");
						} catch (NameNotFoundException exception) {
							System.err.println(exception);
							return null;
						}

						SharedPreferences preference = PreferenceManager
								.getDefaultSharedPreferences(AnswerMessageActivity.this);

						String instanceHash = preference.getString(
								Util.SHARED_PREFERENCE_INSTNCE_HASH_CODE_KEY,
								"");
						String messageHash = Long.toHexString(UUID.randomUUID()
								.getLeastSignificantBits());
						String message = ((EditText) findViewById(R.id.message_write))
								.getText().toString();

						HttpClient client = new DefaultHttpClient();
						client.getParams().setParameter(
								"http.protocol.content-charset", "UTF-8");
						HttpPost post = new HttpPost("http://" + host + "/"
								+ script);

						JSONObject json = new JSONObject();
						try {
							json.put(Util.JSON_INSTNCE_HASH_CODE_KEY,
									instanceHash);
							json.put(Util.JSON_PARENT_HASH_CODE_KEY, parentHash);
							json.put(Util.JSON_MESSAGE_HASH_CODE_KEY,
									messageHash);
							json.put(Util.JSON_MESSAGE_KEY, message);
						} catch (JSONException exception) {
							System.err.println(exception);
						}

						List<NameValuePair> pairs = new ArrayList<NameValuePair>();
						pairs.add(new BasicNameValuePair("replay", json
								.toString()));
						try {
							post.setEntity(new UrlEncodedFormEntity(pairs));
						} catch (UnsupportedEncodingException exception) {
							System.err.println(exception);
						}

						try {
							HttpResponse response = client.execute(post);
						} catch (ClientProtocolException exception) {
							System.err.println(exception);
						} catch (IOException exception) {
							System.err.println(exception);
						}

						return null;
					}
				}).execute();

				Toast.makeText(AnswerMessageActivity.this,
						R.string.request_message_send, Toast.LENGTH_SHORT)
						.show();
				AnswerMessageActivity.this.finish();
			}
		});

		/*
		 * Load message from the remote server.
		 */
		(new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				String host = "";
				try {
					host = getPackageManager().getApplicationInfo(
							AnswerMessageActivity.this.getPackageName(),
							PackageManager.GET_META_DATA).metaData
							.getString("host");
				} catch (NameNotFoundException exception) {
					System.err.println(exception);
					return null;
				}

				String script = "";
				try {
					script = getPackageManager().getActivityInfo(
							AnswerMessageActivity.this.getComponentName(),
							PackageManager.GET_ACTIVITIES
									| PackageManager.GET_META_DATA).metaData
							.getString("script");
				} catch (NameNotFoundException exception) {
					System.err.println(exception);
					return null;
				}

				HttpClient client = new DefaultHttpClient();
				client.getParams().setParameter(
						"http.protocol.content-charset", "UTF-8");
				HttpPost post = new HttpPost("http://" + host + "/" + script);

				JSONObject json = new JSONObject();
				try {
					json.put(Util.JSON_MESSAGE_HASH_CODE_KEY, parentHash);
				} catch (JSONException exception) {
					System.err.println(exception);
				}

				List<NameValuePair> pairs = new ArrayList<NameValuePair>();
				pairs.add(new BasicNameValuePair("get_message", json.toString()));
				try {
					post.setEntity(new UrlEncodedFormEntity(pairs));
				} catch (UnsupportedEncodingException exception) {
					System.err.println(exception);
				}

				try {
					HttpResponse response = client.execute(post);

					JSONObject result = new JSONObject(EntityUtils.toString(
							response.getEntity(), "UTF-8"));

					final String message = result
							.getString(Util.JSON_MESSAGE_KEY);
					boolean found = result.getBoolean(Util.JSON_FOUND_KEY);

					/*
					 * If there is a new message open message read activity (by
					 * sending message hash as parameter).
					 */
					if (found == true) {
						AnswerMessageActivity.this
								.runOnUiThread(new Runnable() {
									public void run() {
										((EditText) findViewById(R.id.message_read))
												.setText(message);
									}
								});
					}
				} catch (ClientProtocolException exception) {
					System.err.println(exception);
				} catch (IOException exception) {
					System.err.println(exception);
				} catch (JSONException exception) {
					System.err.println(exception);
				}

				return null;
			}
		}).execute();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onDestroy() {
		if (helper != null) {
			helper.close();
			helper = null;
		}
		super.onDestroy();
	}
}
