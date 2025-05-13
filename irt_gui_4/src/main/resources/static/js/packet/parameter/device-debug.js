
const deviceDebug = {};

deviceDebug.parameter = {};
deviceDebug.parameter.debugInfo = 1;		/* device information: parts, firmware and etc. */
deviceDebug.parameter.debugDump = 2;		/* dump of registers for specified device index */
deviceDebug.parameter.readWrite = 3;		/* registers read/write operations */
deviceDebug.parameter.index		= 4;		/* device index information print */
deviceDebug.parameter.calibrationMode = 5;	/* calibration mode */
deviceDebug.parameter.environmentIo = 10;	/* operations with environment variables */
deviceDebug.parameter.devices	= 30;
