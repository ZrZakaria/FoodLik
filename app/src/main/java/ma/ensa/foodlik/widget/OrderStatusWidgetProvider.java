package ma.ensa.foodlik.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import ma.ensa.foodlik.MainActivity;
import ma.ensa.foodlik.R;
import ma.ensa.foodlik.data.PreferencesManager;

public class OrderStatusWidgetProvider extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int id : appWidgetIds) {
            updateWidget(context, appWidgetManager, id);
        }
    }

    public static void updateAll(Context context) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        ComponentName component = new ComponentName(context, OrderStatusWidgetProvider.class);
        int[] ids = manager.getAppWidgetIds(component);
        for (int id : ids) updateWidget(context, manager, id);
    }

    private static void updateWidget(Context context, AppWidgetManager manager, int widgetId) {
        PreferencesManager prefs = new PreferencesManager(context);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_order_status);
        int orderId = prefs.getLastOrderId();
        views.setTextViewText(R.id.widgetStatus, orderId == 0 ? context.getString(R.string.widget_empty) : "#" + orderId + " - " + prefs.getLastOrderStatus());
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.widgetStatus, pendingIntent);
        manager.updateAppWidget(widgetId, views);
    }
}
