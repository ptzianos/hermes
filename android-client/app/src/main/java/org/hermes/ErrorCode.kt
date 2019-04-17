package org.hermes

enum class ErrorCode(val errorStr: String) {
    NOT_REGISTERED("not_registered"),
    ALREADY_REGISTERED("already_registered"),
    NO_DATA_ID("no_data_id"),
    NO_TYPE("no_type"),
    NO_UNIT("no_unit"),
    NO_UUID("no_uuid"),
    SEALED("sealed")
}