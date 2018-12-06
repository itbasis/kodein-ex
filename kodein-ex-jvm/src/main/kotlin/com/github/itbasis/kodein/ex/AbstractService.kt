package com.github.itbasis.kodein.ex

import klog.KLoggerHolder
import klog.WithLogging

abstract class AbstractService : Service,
                                 WithLogging by KLoggerHolder()
