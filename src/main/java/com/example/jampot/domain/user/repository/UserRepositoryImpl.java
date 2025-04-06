package com.example.jampot.domain.user.repository;

import com.example.jampot.domain.user.domain.QUser;
import com.example.jampot.domain.user.domain.QUserGenre;
import com.example.jampot.domain.user.domain.QUserSession;
import com.example.jampot.domain.user.domain.User;
import com.nimbusds.oauth2.sdk.util.CollectionUtils;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;


@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<User> searchByAllConditions(String nickname, List<String> sessionNames, List<String> genreNames) {
        QUser user = QUser.user;
        QUserSession userSession = QUserSession.userSession;
        QUserGenre userGenre = QUserGenre.userGenre;

        return queryFactory
                .selectFrom(user)
                .where(
                        nickname != null ? user.nickName.containsIgnoreCase(nickname) : null,
                        !CollectionUtils.isEmpty(sessionNames) ?
                        user.id.in(
                                JPAExpressions.select(userSession.user.id)
                                        .from(userSession)
                                        .where(userSession.session.name.in(sessionNames))
                                        .groupBy(userSession.user.id)
                                        .having(userSession.session.name.countDistinct().eq((long) sessionNames.size()))
                        ) : null,
                        !CollectionUtils.isEmpty(genreNames) ?
                        user.id.in(
                                JPAExpressions.select(userGenre.user.id)
                                        .from(userGenre)
                                        .where(userGenre.genre.name.in(genreNames))
                                        .groupBy(userGenre.user.id)
                                        .having(userGenre.genre.name.countDistinct().eq((long) genreNames.size()))
                        ) : null
                )
                .fetch();
    }
}
