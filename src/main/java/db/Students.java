package db;

import lombok.*;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class Students {
    @Getter private String name;
    private int passportNumber;
    private int passportSeries;
}
