﻿using System;
using System.Net;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Documents;
using System.Windows.Ink;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Animation;
using System.Windows.Shapes;

namespace rho.common
{
    public interface IInputStream
    {
        long available();
        int read();
        int read(byte[] buffer, int bufOffset, int bytesToRead);
        void reset();
    }
}
