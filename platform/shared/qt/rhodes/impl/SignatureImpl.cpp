#include "common/RhoPort.h"
#include "ext/rho/rhoruby.h"
#include "common/RhodesApp.h"
#include "MainWindowImpl.h"

extern "C" {

void rho_signature_take_signature(char* callback_url, char* image_format)
{
    //TODO: rho_signature_take_signature
    //String callback = callback_url;
    //String format = image_format ? image_format : "png"; // default PNG
    //Signature::Params* params = new Signature::Params(callback, format);
    CMainWindow::getInstance()->takeSignature(0);
}

} //extern "C"
