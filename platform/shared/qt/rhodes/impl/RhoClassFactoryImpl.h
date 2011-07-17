#pragma once

#include "common/RhoPort.h"
#include "common/RhoDefs.h"
#include "common/IRhoClassFactory.h"
#ifdef OS_WINDOWS
#ifdef RHO_SYMBIAN
#include "rho/common/RhoThreadImpl.h"
#else // RHO_SYMBIAN
#include "RhoThreadImpl.h"
#endif // RHO_SYMBIAN
#include "rho/net/NetRequestImpl.h"
#define CNETREQUESTIMPL new net::CNetRequestImpl()
#define CRHOTHREADIMPL new CRhoThreadImpl()
#define CRHOCRYPTIMPL NULL
#else
#include "net/CURLNetRequest.h"
#include "common/PosixThreadImpl.h"
// #include "net/iphone/sslimpl.h"
#define CNETREQUESTIMPL new net::CURLNetRequest()
#define CRHOTHREADIMPL new CPosixThreadImpl()
#define CRHOCRYPTIMPL NULL
#endif
// #include "RhoCryptImpl.h"

namespace rho {
namespace common {
		
class CRhoClassFactory : public common::IRhoClassFactory
{
public:
    net::INetRequestImpl* createNetRequestImpl()
    {
        return CNETREQUESTIMPL;
    }

    common::IRhoThreadImpl* createThreadImpl()
    {
        return CRHOTHREADIMPL;
    }

    net::ISSL* createSSLEngine()
    {
        //TODO: createSSLEngine
        return NULL; // new net::SSLImpl();
    }

    IRhoCrypt* createRhoCrypt()
    {
        //TODO: createRhoCrypt
        return CRHOCRYPTIMPL;
    }
};

}
}
