package project.Service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.Entity.Tag;
import project.Entity.TagGroup;
import project.Repository.RepTag;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class TagService {
    private final RepTag repTag;

    public TagService(RepTag repTag){
        this.repTag=repTag;
    }

    public List<Tag> getAll() {
        return repTag.findAll();
    }

    // Получить теги по группе (например, все теги цены)
    public List<Tag> getByTagGroup(TagGroup tagGroup) {
        return repTag.findAll().stream()
                .filter(t -> t.getTagGroup().equals(tagGroup))
                .toList();
    }


    @Transactional
    public Tag save(Tag tag) {
        return repTag.save(tag);
    }
}
