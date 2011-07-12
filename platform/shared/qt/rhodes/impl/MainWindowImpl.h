#pragma once

#include <string>
#include "logging/RhoLog.h"
#include "common/RhoConf.h"
#include "common/RhodesApp.h"
#include "common/rhoparams.h"
#include "common/RhoNativeViewManager.h"
#include "../MainWindowCallback.h"
#undef null
#include <QObject>

class CMainWindow : public QObject, IMainWindowCallback
{
    Q_OBJECT
    DEFINE_LOGCLASS;

public:
    typedef struct _TNavigateData {
        int index;
        wchar_t* url;
    } TNavigateData;

private:
    explicit CMainWindow();
public:
    ~CMainWindow();
    static CMainWindow* getInstance(void);
    // IMainWindowCallback
    virtual void updateSizeProperties(int width, int height);
    virtual void onActivate(int active);
    virtual void logEvent(const ::std::string& message);
    virtual void createCustomMenu(void);
    virtual void onCustomMenuItemCommand(int nItemPos);
    virtual void onWindowClose(void);
    // public methods:
    bool Initialize(const wchar_t* title);
    void DestroyUi(void);
    //TODO: CNativeToolbar& getToolbar(){ return m_toolbar; }
    //TODO: CNativeTabbar& getTabbar(){ return m_tabbar; }
    // for 'main_window_closed' System property
    static bool mainWindowClosed;

    // proxy methods:
    bool init(IMainWindowCallback* callback, const wchar_t* title);
    void setCallback(IMainWindowCallback* callback);
    void messageLoop(void);
    void navigate(const wchar_t* url, int index);
    void GoBack(void);
    void GoForward(void);
    void Refresh(int index);
    // toolbar/tabbar
    bool isStarted();
    // toolbar proxy
    int getToolbarHeight();
    void createToolbar(rho_param *p);
    void removeToolbar();
    void removeAllButtons();
    // menu proxy
    void menuClear();
    void menuAddSeparator();
    void menuAddAction(const char* label, int item);
    // tabbar
    int getTabbarHeight();
    void removeAllTabs(bool restore);
    void createTabbar(int bar_type, rho_param *p);
    void removeTabbar();
    void tabbarSwitch(int index);
    void tabbarBadge(int index, char* badge);
    int tabbarGetCurrent();

private:
    //TODO: CLogView m_logView;
    //TODO: CNativeToolbar m_toolbar;
    //TODO: CNativeTabbar m_tabbar;
    bool m_started;
    void* qtMainWindow;
    void* qtApplication;

private:
    static int m_screenWidth;
    static int m_screenHeight;
    
public:
    static int getScreenWidth() {return m_screenWidth;}
    static int getScreenHeight() {return m_screenHeight;}

private:
    rho::Vector<rho::common::CAppMenuItem> m_arAppMenuItems;
    //TODO: CAlertDialog *m_alertDialog;
    //TODO: CSyncStatusDlg *m_SyncStatusDlg;

public:
    void exitCommand(void);
    void navigateBackCommand(void);
    void navigateForwardCommand(void);
    void logCommand(void);
    void refreshCommand(int);
    void navigateCommand(TNavigateData*);
    void takePicture(const char*);
    void selectPicture(const char*);
    void alertShowPopup(void *); // CAlertDialog::Params *
    void alertHidePopup(void);
    void dateTimePicker(void *); //TODO: CDateTimeMessage *
    void executeCommand(RhoNativeViewRunnable*);
    void executeRunnable(rho::common::IRhoRunnable*);
    void takeSignature(void*); //TODO: Signature::Params*
    void fullscreenCommand(int);

signals:
    void doExitCommand(void);
    void doNavigateBackCommand(void);
    void doNavigateForwardCommand(void);
    void doLogCommand(void);
    void doRefreshCommand(int);
    void doNavigateCommand(TNavigateData*);
    void doTakePicture(const char*);
    void doSelectPicture(const char*);
    void doAlertShowPopup(void *); // CAlertDialog::Params *
    void doAlertHidePopup(void);
    void doDateTimePicker(void *); //TODO: CDateTimeMessage *
    void doExecuteCommand(RhoNativeViewRunnable*);
    void doExecuteRunnable(rho::common::IRhoRunnable*);
    void doTakeSignature(void*); //TODO: Signature::Params*
    void doFullscreenCommand(int);
};
