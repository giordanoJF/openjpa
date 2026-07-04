package org.apache.openjpa.kernel;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;

import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.enhance.PersistenceCapable;
import org.apache.openjpa.enhance.StateManager;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.MetaDataRepository;
import org.apache.openjpa.util.ApplicationIds;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.MockedStatic;

/**
 * Test BB: isDetached(Object obj, boolean find). Tuple e oracolo da
 * report/data/isDetached_combinazioni.csv (10 righe).
 */
@RunWith(Parameterized.class)
public class IsDetachedTest {

    private enum ObjKind { NULL, GESTITO, NON_GESTITO }

    @Parameters(name = "{index}: {0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
            {"obj=null, find=true", ObjKind.NULL, true, null, false},
            {"obj=null, find=false", ObjKind.NULL, false, null, false},
            {"obj=gestito, find=true, store=presente", ObjKind.GESTITO, true, Boolean.TRUE, false},
            {"obj=gestito, find=true, store=non presente", ObjKind.GESTITO, true, Boolean.FALSE, false},
            {"obj=gestito, find=false, store=presente", ObjKind.GESTITO, false, Boolean.TRUE, false},
            {"obj=gestito, find=false, store=non presente", ObjKind.GESTITO, false, Boolean.FALSE, false},
            {"obj=non gestito, find=false, store=presente", ObjKind.NON_GESTITO, false, Boolean.TRUE, false},
            {"obj=non gestito, find=false, store=non presente", ObjKind.NON_GESTITO, false, Boolean.FALSE, false},
            {"obj=non gestito, find=true, store=presente", ObjKind.NON_GESTITO, true, Boolean.TRUE, true},
            {"obj=non gestito, find=true, store=non presente", ObjKind.NON_GESTITO, true, Boolean.FALSE, false},
        });
    }

    private final ObjKind objKind;
    private final boolean find;
    private final Boolean storePresente;
    private final boolean expected;

    private BrokerImpl broker;
    private MetaDataRepository repo;

    public IsDetachedTest(String label, ObjKind objKind, boolean find, Boolean storePresente, boolean expected) {
        this.objKind = objKind;
        this.find = find;
        this.storePresente = storePresente;
        this.expected = expected;
    }

    @Before
    public void setUp() {
        AbstractBrokerFactory factory = mock(AbstractBrokerFactory.class, RETURNS_DEEP_STUBS);
        StoreManager storeManager = mock(StoreManager.class, RETURNS_DEEP_STUBS);
        DelegatingStoreManager delegatingStoreManager = new DelegatingStoreManager(storeManager) { };
        OpenJPAConfiguration conf = mock(OpenJPAConfiguration.class, RETURNS_DEEP_STUBS);
        repo = mock(MetaDataRepository.class, RETURNS_DEEP_STUBS);
        when(conf.getMetaDataRepositoryInstance()).thenReturn(repo);
        when(factory.getConfiguration()).thenReturn(conf);

        broker = new BrokerImpl();
        broker.initialize(factory, delegatingStoreManager, false, ConnectionRetainModes.CONN_RETAIN_DEMAND, false);
    }

    private Object buildObj() {
        switch (objKind) {
            case NULL:
                return null;
            case GESTITO: {
                PersistenceCapable pc = mock(PersistenceCapable.class);
                when(pc.pcGetStateManager()).thenReturn(mock(StateManager.class));
                when(pc.pcIsDetached()).thenReturn(Boolean.FALSE);
                return pc;
            }
            case NON_GESTITO: {
                PersistenceCapable pc = mock(PersistenceCapable.class);
                when(pc.pcGetStateManager()).thenReturn(null);
                when(pc.pcIsDetached()).thenReturn(null);
                return pc;
            }
            default:
                throw new IllegalStateException();
        }
    }

    @Test
    public void testIsDetached() {
        Object obj = buildObj();
        boolean result;

        if (objKind == ObjKind.NON_GESTITO && find) {
            // Il ramo "last resort" richiama find(), un altro metodo bersaglio: lo si tratta
            // come collaboratore gia' testato a se' (spy), non lo si riverifica qui.
            BrokerImpl spyBroker = spy(broker);
            ClassMetaData meta = mock(ClassMetaData.class);
            when(repo.getMetaData(any(Class.class), any(), eq(true))).thenReturn(meta);
            Object dummyOid = new Object();
            try (MockedStatic<ApplicationIds> appIds = mockStatic(ApplicationIds.class)) {
                appIds.when(() -> ApplicationIds.create(any(), any())).thenReturn(dummyOid);
                doReturn(Boolean.TRUE.equals(storePresente) ? new Object() : null)
                    .when(spyBroker).find(eq(dummyOid), isNull(), any(java.util.BitSet.class), isNull(), eq(0));
                result = spyBroker.isDetached(obj, find);
            }
        } else {
            result = broker.isDetached(obj, find);
        }

        assertEquals(expected, result);
    }
}
