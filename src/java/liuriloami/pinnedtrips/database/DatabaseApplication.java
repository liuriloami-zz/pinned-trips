package liuriloami.pinnedtrips.database;

import android.app.Application;

public class DatabaseApplication extends Application {

    DatabaseManager databaseManager;

    public void openDatabase() {
        this.databaseManager = new DatabaseManager(this);
        this.databaseManager.openDatase();
    }

    public DatabaseManager getDatabase() {
        return this.databaseManager;
    }
}
