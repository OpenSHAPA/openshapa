/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.datavyu.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.datavyu.Datavyu;

import java.io.File;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;


/**
 * Ok this curly piece of work is a bit body of reflection to basically achieve
 * the following snippet of code that will ultimately compile on any platform.
 * <p/>
 * public class MacOSAboutHandler extends Application {
 * <p/>
 * public MacOSAboutHandler() { addApplicationListener(new AboutBoxHandler()); }
 * <p/>
 * class AboutBoxHandler extends ApplicationAdapter { public void
 * handleAbout(ApplicationEvent event) {
 * Datavyu.getApplication().showAboutWindow(); event.setHandled(true); } } }
 */
public class MacHandler {

    /** The logger for this class */
    private static Logger logger = LogManager.getLogger(MacHandler.class);

    public static int getOSVersion() {
        try {
            String osVersion = System.getProperty("os.version");
            return Integer.valueOf(osVersion.split("\\.")[1]); // get major version
        } catch (Exception e) {
            logger.error("Could not get major version number", e);
        }
        return -1;
    }

    /**
     * Default constructor.
     */
    public MacHandler() {
        try {
            Class appc = Class.forName("com.apple.eawt.Application");
            Object app = appc.newInstance();
            Class applc = Class.forName("com.apple.eawt.ApplicationListener");
            Object listener = Proxy.newProxyInstance(Thread.currentThread()
                    .getContextClassLoader(), new Class[]{applc},
                    new HandlerForApplicationAdapter());
            // Add the listener to the application.
            Method m = appc.getMethod("addApplicationListener", applc);
            m.invoke(app, listener);
        } catch (ClassNotFoundException e) {
            logger.error("Unable to find apple classes", e);
        } catch (InstantiationException e) {
            logger.error("Unable to instantiate apple application", e);
        } catch (IllegalAccessException e) {
            logger.error("Unable to access application excapeion", e);
        } catch (NoSuchMethodException e) {
            logger.error("Unable to access method in application", e);
        } catch (InvocationTargetException e) {
            logger.error("Unable to invocate target", e);
        }
    }

    /**
     * InvocationHandler for the ApplicationAdapter to override some methods.
     */
    class HandlerForApplicationAdapter implements InvocationHandler {

        /**
         * Called when a method in the proxy object is being invoked.
         *
         * @param proxy  The object we are proxying.
         * @param method The method that is being invoked.
         * @param args   The arguments being supplied to the method.
         * @return Value for the method being invoked.
         */
        public Object invoke(final Object proxy, final Method method,
                             final Object[] args) {
            try {
                Class ae = Class.forName("com.apple.eawt.ApplicationEvent");
                Method setHandled;
                String methodName = method.getName();
                switch (methodName) {
                    case "handleAbout":
                        Datavyu.getApplication().showAboutWindow();
                        setHandled = ae.getMethod("setHandled", boolean.class);
                        setHandled.invoke(args[0], true);
                        break;
                    case "handleSupportOpen":
                        Datavyu.getApplication().openSupportSite();
                        setHandled = ae.getMethod("setHandled", boolean.class);
                        setHandled.invoke(args[0], true);
                        break;
                    case "handleGuideOpen":
                        Datavyu.getApplication().openGuideSite();
                        setHandled = ae.getMethod("setHandled", boolean.class);
                        setHandled.invoke(args[0], true);
                        break;
                    case "handleUpdate":
                        Datavyu.getApplication().showUpdateWindow();
                        setHandled = ae.getMethod("setHandled", boolean.class);
                        setHandled.invoke(args[0], true);
                        break;
                    case "handleQuit":
                        boolean shouldQuit = Datavyu.getApplication().safeQuit();
                        if (shouldQuit) {
                            Datavyu.getApplication().getMainFrame().setVisible(false);
                            Datavyu.getApplication().shutdown();
                        }
                        setHandled = ae.getMethod("setHandled", boolean.class);
                        setHandled.invoke(args[0], shouldQuit);
                        break;
                    case "handleOpenFile":
                        Method getFilename = ae.getMethod("getFilename", null);
                        String fileName = (String) getFilename.invoke(args[0],null);
                        if (Datavyu.getApplication().ready) {
                            Datavyu.getApplication().getView().openExternalFile(new File(fileName));
                        } else {
                            Datavyu.getApplication().setCommandLineFile(fileName);
                        }
                        setHandled = ae.getMethod("setHandled", boolean.class);
                        setHandled.invoke(args[0], true);
                        break;
                    default:
                        logger.info("Tried to invoke method " + methodName + " which is not registered");
                }
            } catch (NoSuchMethodException e) {
                logger.error("Unable to access method in application", e);
            } catch (IllegalAccessException e) {
                logger.error("Unable to access application excapeion", e);
            } catch (InvocationTargetException e) {
                logger.error("Unable to invocate target", e);
            } catch (ClassNotFoundException e) {
                logger.error("Unable to find apple classes", e);
            }
            return null;
        }
    }
}
