package mj.carthy.easyutils.document

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.springframework.data.annotation.*
import java.time.Instant

open class BaseDocument<T> (
  @JsonIgnoreProperties
  @ReadOnlyProperty
  @Id
  open var id: T? = null,

  @JsonIgnoreProperties
  @JsonIgnore
  @CreatedBy
  open var createdBy: String? = null,

  @JsonIgnoreProperties
  @JsonIgnore
  @LastModifiedBy
  open var lastModifiedBy: String? = null,

  @JsonIgnoreProperties
  @JsonIgnore
  @CreatedDate
  open var createdDate: Instant? = null,

  @JsonIgnoreProperties
  @JsonIgnore
  @LastModifiedDate
  open var lastModifiedDate: Instant? = null,

  @JsonIgnoreProperties
  @JsonIgnore
  @Version
  open var version: Number? = null
)