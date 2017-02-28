package artful.entities;

import javax.persistence.*;

@Entity
@Table(name = "corpora")
public class Corpora {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String full_name;
    private String lang;
    private Integer size;
    private Integer year;
    private String source;
}
