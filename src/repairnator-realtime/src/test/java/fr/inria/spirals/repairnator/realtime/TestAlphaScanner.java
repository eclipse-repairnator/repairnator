package fr.inria.spirals.repairnator.realtime;

import fr.inria.jtravis.entities.Build;
import fr.inria.jtravis.entities.Commit;
import fr.inria.jtravis.entities.v2.BuildV2;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class TestAlphaScanner {

    Build build;
    BuildV2 buildV2;

    @Mock
    BuildHelperV2 buildHelper;
    @Mock
    RTScanner rtScanner;
    @Mock
    SequencerCollector collector;

    @Spy
    @InjectMocks
    AlphaScanner scanner = new AlphaScanner();

    @Before
    public void setup() {
        build = new Build();
        buildV2 = new BuildV2();
        buildV2.setCommit(new Commit());

        MockitoAnnotations.initMocks(this);

        when(buildHelper.fromId(anyLong())).thenReturn(Optional.of(build));
        when(buildHelper.fromIdV2(anyLong())).thenReturn(Optional.of(buildV2));

        scanner.setup();
    }

    @Test
    public void TestAttemptJob () {
        scanner.attemptJob(702053045); //failing job
        verify(rtScanner, times(1)).submitBuildToExecution(any(Build.class));
    }

    @Test
    public void TestCollectJob () {
        scanner.collectJob(704352008, "javierron/failingProject"); //passing job
        verify(collector, times(1)).handle(anyString(), anyString());
    }

}
