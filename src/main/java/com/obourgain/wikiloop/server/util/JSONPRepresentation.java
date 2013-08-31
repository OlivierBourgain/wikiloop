package com.obourgain.wikiloop.server.util;


import org.apache.log4j.Logger;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.engine.io.BioUtils;
import org.restlet.engine.io.NioUtils;
import org.restlet.representation.Representation;
import org.restlet.util.WrapperRepresentation;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

/**
 * Thanks to https://bitbucket.org/markkharitonov/restlet-jsonp-filter/wiki/Home
 */
public class JSONPRepresentation extends WrapperRepresentation {
    private static final Logger log = Logger.getLogger(JSONPRepresentation.class);
    private final String m_callback;
    private final Status m_status;

    public JSONPRepresentation(String callback, Status status, Representation wrappedRepresentation) {
        super(wrappedRepresentation);
        m_callback = callback;
        m_status = status;
    }

    private long newSize = -2L;

    @Override
    public long getSize() {
        if (newSize == -2L) {
            log.trace("Updating size");
            long result = super.getSize();
            if (result > 0 && MediaType.APPLICATION_JSON.equals(super.getMediaType())) {
                result = result + m_callback.length() + "({status:,body:});".length() + Integer.toString(m_status.getCode()).length();
                newSize = result;
            } else {
                newSize = UNKNOWN_SIZE;
            }
        }
        log.trace("Size is " + newSize);
        setSize(newSize);
        return newSize;
    }

    @Override
    public ReadableByteChannel getChannel() throws IOException {
        return NioUtils.getChannel(getStream());
    }

    @Override
    public InputStream getStream() throws IOException {
        return BioUtils.getStream(this);
    }

    @Override
    public String getText() throws IOException {
        return BioUtils.toString(getStream());
    }

    @Override
    public void write(OutputStream outputStream) throws IOException {
        outputStream.write(m_callback.getBytes());
        outputStream.write("({status:".getBytes());
        outputStream.write(Integer.toString(m_status.getCode()).getBytes());
        outputStream.write(",body:".getBytes());
        if (MediaType.APPLICATION_JSON.equals(super.getMediaType())) {
            BioUtils.copy(super.getStream(), outputStream);
        } else {
            outputStream.write("'".getBytes());
            String text = super.getText();
            if (text.indexOf('\'') >= 0) {
                text = text.replaceAll("\'", "\\\'");
            }
            outputStream.write(text.getBytes());
            outputStream.write("'".getBytes());
        }
        outputStream.write("});".getBytes());
    }

    @Override
    public void write(WritableByteChannel writableChannel) throws IOException {
        write(NioUtils.getStream(writableChannel));
    }

    @Override
    public MediaType getMediaType() {
        return MediaType.APPLICATION_JAVASCRIPT;
    }
}