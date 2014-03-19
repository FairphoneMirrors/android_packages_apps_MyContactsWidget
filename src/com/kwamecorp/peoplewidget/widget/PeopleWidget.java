package com.kwamecorp.peoplewidget.widget;

import com.kwamecorp.peoplewidget.R;
import com.kwamecorp.peoplewidget.data.ContactInfo;
import com.kwamecorp.peoplewidget.data.ContactInfoManager;
import com.kwamecorp.peoplewidget.data.PeopleManager;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RemoteViews;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PeopleWidget extends AppWidgetProvider
{

    private static final String TAG = PeopleWidget.class.getSimpleName();

    // List of custom views that contains the contact photo and contact name
    private int[] mContainerImage = {
            R.id.contact_photo_1, R.id.contact_photo_2, R.id.contact_photo_3, R.id.contact_photo_4, R.id.contact_photo_5, R.id.contact_photo_6,
    };
    private int[] mContainerName = {
            R.id.contact_name_1, R.id.contact_name_2, R.id.contact_name_3, R.id.contact_name_4, R.id.contact_name_5, R.id.contact_name_6,
    };
    private int[] mContainerPhone = {
            R.id.contact_phone_1, R.id.contact_phone_2, R.id.contact_phone_3, R.id.contact_phone_4, R.id.contact_phone_5, R.id.contact_phone_6,
    };
    private int[] mContainerSms = {
            R.id.contact_sms_1, R.id.contact_sms_2, R.id.contact_sms_3, R.id.contact_sms_4, R.id.contact_sms_5, R.id.contact_sms_6,
    };

    // List of custom views that contains the contact photo and contact name
    private int[] mContainerImage2 = {
            R.id.contact_photo_7, R.id.contact_photo_8, R.id.contact_photo_9, R.id.contact_photo_10,
    };
    private int[] mContainerName2 = {
            R.id.contact_name_7, R.id.contact_name_8, R.id.contact_name_9, R.id.contact_name_10,
    };
    private int[] mContainerPhone2 = {
            R.id.contact_phone_7, R.id.contact_phone_8, R.id.contact_phone_9, R.id.contact_phone_10,
    };
    private int[] mContainerSms2 = {
            R.id.contact_sms_7, R.id.contact_sms_8, R.id.contact_sms_9, R.id.contact_sms_10,
    };

    private RemoteViews mViews;
    private Context mContext;

    @Override
    public void onEnabled(Context context)
    {
        super.onEnabled(context);

        mViews = new RemoteViews(context.getPackageName(), R.layout.favourite_access_widget);
        mContext = context;
        updateView();
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
    {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        mViews = new RemoteViews(context.getPackageName(), R.layout.favourite_access_widget);
        mContext = context;
        Log.i(this.getClass().getSimpleName(), "onUpdate()");
        updateView();
    }

    private void updateView()
    {
        Log.i(TAG, "updateView()");
        updateBoard();
    }

    private void updateBoard()
    {
        new AsyncGetContacts().execute(new String[] {
            null
        });
    }

    private void updateImage(final int view, final String photoUrl)
    {
        if (photoUrl != null)
        {
            Bitmap bitmap = loadContactPhoto(photoUrl, mContext);
            mViews.setImageViewBitmap(view, bitmap);
        }
        else
        {
            mViews.setImageViewResource(view, android.R.drawable.sym_def_app_icon);
        }
    }

    private Bitmap loadContactPhoto(final String photoData, Context context)
    {
        Uri thumbUri;
        AssetFileDescriptor afd = null;

        try
        {
            thumbUri = Uri.parse(photoData);
            /*
             * Retrieves an AssetFileDescriptor object for the thumbnail URI
             * using ContentResolver.openAssetFileDescriptor
             */
            afd = context.getContentResolver().openAssetFileDescriptor(thumbUri, "r");
            /*
             * Gets a file descriptor from the asset file descriptor. This
             * object can be used across processes.
             */
            FileDescriptor fileDescriptor = afd.getFileDescriptor();
            // Decode the photo file and return the result as a Bitmap
            // If the file descriptor is valid
            if (fileDescriptor != null)
            {
                // Decodes the bitmap
                Log.i(this.getClass().getSimpleName(), "Uri = " + thumbUri.toString());
                return BitmapFactory.decodeFileDescriptor(fileDescriptor, null, null);
            }
            // If the file isn't found
        } catch (FileNotFoundException e)
        {

        } finally
        {

            if (afd != null)
            {
                try
                {
                    afd.close();
                } catch (IOException e)
                {
                }
            }
        }
        return null;
    }

    private List<ContactInfo> getLocalFrequentContacts()
    {
        return new ArrayList<ContactInfo>();
    }

    private class AsyncGetContacts extends AsyncTask<String, Void, List<ContactInfo>>
    {

        @Override
        protected List<ContactInfo> doInBackground(String... in)
        {
            return makeMeRequest();
        }

        private List<ContactInfo> makeMeRequest()
        {
            return getLocalFrequentContacts();
        }

        @Override
        protected void onPostExecute(final List<ContactInfo> result)
        {
            if (result == null)
            {
                return;
            }

            ContactInfoManager instance = PeopleManager.getInstance();

            List<ContactInfo> mostContacted = new ArrayList<ContactInfo>(instance.getMostContacted());
            for (int i = 0; i < mContainerImage.length && i < (mostContacted.size()); i++)
            {
                updateImage(mContainerImage[i], mostContacted.get(i).photoUri);
                String contactName = TextUtils.isEmpty(mostContacted.get(i).name) ? mostContacted.get(i).phoneNumber : mostContacted.get(i).name;
                mViews.setTextViewText(mContainerName[i], contactName);
                // open contact
                addOpenContactBehaviour(mContainerName, mostContacted, i);
                // call contact
                addCallContactBehaviour(mContainerPhone, mostContacted, i);
                // sms contact
                addSmsContactBehaviour(mContainerSms, mostContacted, i);
            }

            List<ContactInfo> lastContacted = new ArrayList<ContactInfo>(instance.getLastContacted());

            for (int i = 0; i < mContainerImage2.length && i < (lastContacted.size()); i++)
            {
                updateImage(mContainerImage2[i], lastContacted.get(i).photoUri);
                String contactName = TextUtils.isEmpty(lastContacted.get(i).name) ? lastContacted.get(i).phoneNumber : lastContacted.get(i).name;
                mViews.setTextViewText(mContainerName2[i], contactName);
                // open contact
                addOpenContactBehaviour(mContainerName2, lastContacted, i);
                // call contact
                addCallContactBehaviour(mContainerPhone2, lastContacted, i);
                // sms contact
                addSmsContactBehaviour(mContainerSms2, lastContacted, i);
            }

            ComponentName widget = new ComponentName(mContext, PeopleWidget.class);
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(mContext);
            appWidgetManager.updateAppWidget(widget, null);
            appWidgetManager.updateAppWidget(widget, mViews);

        }

        public void addSmsContactBehaviour(int[] containerSms, final List<ContactInfo> result, int i)
        {
            String uriSms = "smsto:" + result.get(i).phoneNumber;
            Intent intentSms = new Intent(Intent.ACTION_SENDTO);
            intentSms.setData(Uri.parse(uriSms));

            PackageManager packageManager = mContext.getPackageManager();
            List<ResolveInfo> list = packageManager.queryIntentActivities(intentSms, 0);
            Log.i(this.getClass().getSimpleName(), "CENAS ->" + list.size());
            for (ResolveInfo resolveInfo : list)
            {

                Log.i(this.getClass().getSimpleName(), resolveInfo.activityInfo.packageName + " " + resolveInfo.activityInfo.name);

                if (resolveInfo.activityInfo.name.equals("com.android.mms.ui.ComposeMessageActivity"))
                {
                    ComponentName comp = new ComponentName("com.android.mms", "com.android.mms.ui.ComposeMessageActivity");
                    intentSms.setComponent(comp);
                }
                else if (resolveInfo.activityInfo.name.equals("com.android.mms.ui.ConversationComposer"))
                {
                    ComponentName comp = new ComponentName("com.android.mms", "com.android.mms.ui.ConversationComposer");
                    intentSms.setComponent(comp);
                }
            }
            PendingIntent pendingIntentSms = PendingIntent.getActivity(mContext, 0 /*
                                                                                    * no
                                                                                    * requestCode
                                                                                    */, intentSms, PendingIntent.FLAG_UPDATE_CURRENT /*
                                                                                                                                      * 0
                                                                                                                                      * no
                                                                                                                                      * flags
                                                                                                                                      */);
            mViews.setOnClickPendingIntent(containerSms[i], pendingIntentSms);
        }

        public void addCallContactBehaviour(int[] containerPhone, final List<ContactInfo> result, int i)
        {

            String uriCall = "tel:" + result.get(i).phoneNumber;
            Intent intentCall = new Intent(Intent.ACTION_CALL);

            ComponentName comp = new ComponentName("com.android.phone", "com.android.phone.OutgoingCallBroadcaster");
            intentCall.setComponent(comp);
            intentCall.setData(Uri.parse(uriCall));
            PendingIntent pendingIntentCall = PendingIntent.getActivity(mContext, 0 /*
                                                                                     * no
                                                                                     * requestCode
                                                                                     */, intentCall, PendingIntent.FLAG_UPDATE_CURRENT /*
                                                                                                                                        * 0
                                                                                                                                        * no
                                                                                                                                        * flags
                                                                                                                                        */);
            mViews.setOnClickPendingIntent(containerPhone[i], pendingIntentCall);

            // String uriCall = "tel:" + result.get(i).phoneNumbers;
            // Intent intentCall = new Intent(Intent.ACTION_CALL);
            // intentCall.setData(Uri.parse(uriCall));
            // PendingIntent pendingIntentCall =
            // PendingIntent.getActivity(mContext,
            // 0 /* no requestCode */, intentCall,
            // PendingIntent.FLAG_UPDATE_CURRENT /*0 no flags*/);
            // mViews.setOnClickPendingIntent(mContainerPhone[i],
            // pendingIntentCall);
        }

        public void addOpenContactBehaviour(int[] containerImage, final List<ContactInfo> result, int i)
        {
            if (!TextUtils.isEmpty(result.get(i).contactId))
            {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, "" + result.get(i).contactId));
                PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0 /*
                                                                                     * no
                                                                                     * requestCode
                                                                                     */, intent, PendingIntent.FLAG_UPDATE_CURRENT /*
                                                                                                                                    * 0
                                                                                                                                    * no
                                                                                                                                    * flags
                                                                                                                                    */);
                mViews.setOnClickPendingIntent(containerImage[i], pendingIntent);
            }
            else
            {
                mViews.setOnClickPendingIntent(containerImage[i], null);
            }
        }
    }
}