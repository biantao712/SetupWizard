LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_STATIC_JAVA_LIBRARIES += \
    asusSetupWizard_libV13

LOCAL_AAPT_INCLUDE_ALL_RESOURCES := true

LOCAL_SRC_FILES := $(call all-java-files-under, src)
#LOCAL_SRC_FILES += src/com/asus/ime/api/ISelectInputMode.aidl

#LOCAL_STATIC_JAVA_LIBRARIES += asus-common-ui
LOCAL_RESOURCE_DIR := \
    $(LOCAL_PATH)/res

#LOCAL_AAPT_FLAGS := --auto-add-overlay
#LOCAL_AAPT_FLAGS += --extra-packages

LOCAL_MODULE_TAGS := optional
LOCAL_OVERRIDES_PACKAGES := Provision
LOCAL_PACKAGE_NAME := AsusCnSetupWizard
LOCAL_CERTIFICATE := platform
LOCAL_PROGUARD_FLAG_FILES := proguard.flags
include $(BUILD_PACKAGE)

##################################################
include $(CLEAR_VARS)
LOCAL_MODULE_TAGS := optional
LOCAL_PRELINK_MODULE:= false

# Version : 3.0 beta 5
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := asusSetupWizard_libV13:/libs/android-support-v13.jar

include $(BUILD_MULTI_PREBUILT)
##################################################

# Build other projects
include $(call all-makefiles-under,$(LOCAL_PATH))
