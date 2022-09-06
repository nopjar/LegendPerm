package net.playlegend.sign;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import net.playlegend.LegendPerm;
import net.playlegend.domain.Group;
import net.playlegend.service.ServiceRegistry;
import org.junit.jupiter.api.Test;

class GroupSignListenerTest {

    @Test
    void updateOnPrefix() throws SQLException {
        LegendPerm plugin = mock(LegendPerm.class);
        Group group = mock(Group.class);
        ServiceRegistry serviceRegistry = mock(ServiceRegistry.class);
        SignService signService = mock(SignService.class);

        when(plugin.getServiceRegistry())
                .thenReturn(serviceRegistry);
        when(serviceRegistry.get(SignService.class))
                .thenReturn(signService);

        new GroupSignListener(plugin).update(Group.Operation.PREFIX_CHANGE, group);

        verify(signService).updateAllSignsForGroup(group);
    }

    @Test
    void dontUpdateOnAnythingElse() throws SQLException {
        LegendPerm plugin = mock(LegendPerm.class);
        Group group = mock(Group.class);
        ServiceRegistry serviceRegistry = mock(ServiceRegistry.class);
        SignService signService = mock(SignService.class);

        when(plugin.getServiceRegistry())
                .thenReturn(serviceRegistry);
        when(serviceRegistry.get(SignService.class))
                .thenReturn(signService);

        new GroupSignListener(plugin).update(Group.Operation.SUFFIX_CHANGE, group);

        verifyNoInteractions(signService);
    }

}