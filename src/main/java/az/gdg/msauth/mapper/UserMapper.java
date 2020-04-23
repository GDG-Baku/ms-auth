package az.gdg.msauth.mapper;

import az.gdg.msauth.model.entity.UserEntity;
import az.gdg.msauth.model.dto.UserInfoForBlogService;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;


@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    UserInfoForBlogService entityToDto(UserEntity userEntity);

}
