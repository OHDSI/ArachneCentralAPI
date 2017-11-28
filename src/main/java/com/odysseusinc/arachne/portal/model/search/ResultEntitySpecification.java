package com.odysseusinc.arachne.portal.model.search;

import com.odysseusinc.arachne.portal.model.ArachneFile_;
import com.odysseusinc.arachne.portal.model.ResultEntity;
import com.odysseusinc.arachne.portal.model.ResultEntity_;
import com.odysseusinc.arachne.portal.model.ResultFile;
import com.odysseusinc.arachne.portal.model.AbstractResultFile_;
import com.odysseusinc.arachne.portal.model.Submission;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.criteria.CompoundSelection;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

public class ResultEntitySpecification<T extends ResultEntity> implements Specification<T> {

    private final ResultFileSearch criteria;

    public ResultEntitySpecification(@NotNull ResultFileSearch criteria) {

        this.criteria = criteria;
    }

    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {

        List<Predicate> predicates = new ArrayList<>();

        // Columns

        final Path<Submission> submission = root.get(AbstractResultFile_.submission);
        final Path<String> realName = root.get(AbstractResultFile_.realName);

        Expression<String> relativeRealName = cb.substring(
                root.get(ResultEntity_.realName),
                criteria.getPath().equals("/") ? 1 : criteria.getPath().length() + 1
        );

        // Where
        predicates.add(cb.equal(submission, criteria.getSubmission()));

        if (StringUtils.isNotEmpty(criteria.getPath())) {
            predicates.add(cb.notLike(relativeRealName, "%/%"));
        }

        if (!criteria.getPath().equals("/")) {
            predicates.add(cb.like(realName, criteria.getPath() + "%"));
        }

        if (criteria.getRealName() != null) {
            predicates.add(cb.equal(realName, criteria.getRealName()));
        }

        // Order

        // todo

        return cb.and(predicates.toArray(new Predicate[predicates.size()]));
    }

    public CompoundSelection getSelection(Root<T> root, CriteriaBuilder cb) {

        return cb.construct(ResultEntity.class,
                root.get(ArachneFile_.uuid),
                root.get(ArachneFile_.label),
                root.get(ArachneFile_.realName),
                cb.literal(StringUtils.defaultIfEmpty(criteria.getPath(), "")),
                root.get(ArachneFile_.contentType),
                root.get(ArachneFile_.created),
                root.get(ArachneFile_.updated),
                root.get(AbstractResultFile_.submission),
                root.get(AbstractResultFile_.manuallyUploaded)
        );
    }
}
