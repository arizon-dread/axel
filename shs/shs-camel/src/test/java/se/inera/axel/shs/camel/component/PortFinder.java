package se.inera.axel.shs.camel.component;

import org.eclipse.jetty.server.Server;

class PortFinder {

    /**
     * Find a free tcp port usable by jetty.
     * Note that the port isn't currently allocated and can be claimed by someone else short after this method returns.
     *
     * @return an availbale port
     */
    public static int findFreePort() {
        try {
            Server server = new Server(0);
            server.start();
            while (!server.isStarted()) {
                Thread.sleep(100);
            }
            int port = server.getConnectors()[0].getLocalPort();
            server.stop();
            while (!server.isStopped()) {
                Thread.sleep(100);
            }
            server.destroy();
            return port;

        } catch (Exception e) {
            throw new RuntimeException("Can't find available port", e);
        }
    }
}
