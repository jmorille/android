package net.kindleit.gae.example.server;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Collection;

import net.kindleit.gae.example.model.Message;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests {@link MessageRepository} class.
 * 
 * @author androns
 */
public class MessageRepositoryTest extends LocalDatastoreTest {

    private MessageRepository messageRepository;

    /**
     * @see LocalDatastoreTest#setUp()
     */
    @Before
    @Override
    public void setUp() {
        super.setUp();
        messageRepository = new MessageRepository();
    }

    /**
     * 
     */
    @Test
    public void smokeTest() {
        assertNotNull(messageRepository);

        // create
        Message message = new Message();
        message.setText("Test message");

        messageRepository.create(message);

        // read
        Collection<Message> messages = messageRepository.getAll();

        assertNotNull(messages);
        assertEquals(1, messages.size());
        Message storedMessage = messages.iterator().next();

        assertNotNull(storedMessage.getId());
        assertEquals(message.getText(), storedMessage.getText());

        // delete
        messageRepository.deleteById(storedMessage.getId());

        messages = messageRepository.getAll();
        assertEquals(0, messages.size());
    }
}
