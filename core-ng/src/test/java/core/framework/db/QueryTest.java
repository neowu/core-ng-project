package core.framework.db;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;

/**
 * @author neo
 */
@ExtendWith(MockitoExtension.class)
class QueryTest<T> {
    @Mock
    Query<T> query;

    @BeforeEach
    void createQuery() {
        Mockito.doCallRealMethod().when(query).in(any(), anyList());
    }

    @Test
    void in() {
        assertThatThrownBy(() -> query.in(null, List.of()))
                .hasMessageContaining("field must not be null");

        assertThatThrownBy(() -> query.in("id", List.of()))
                .hasMessageContaining("params must not be empty");

        query.in("id", List.of("1"));
        verify(query).where("id IN (?)", "1");

        query.in("id", List.of("1", "2"));
        verify(query).where("id IN (?, ?)", "1", "2");

        query.in("id", List.of("1", "2", "3"));
        verify(query).where("id IN (?, ?, ?)", "1", "2", "3");
    }
}
