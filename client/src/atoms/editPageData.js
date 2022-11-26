import { atom } from "recoil";
import { v1 } from "uuid";

export const editTrainStationState = atom({
  key: `editTrainStation/${v1()}`,
  default: "기차역",
});

export const editAdressState = atom({
  key: `editAdress/${v1()}`,
  default: "주소",
});

export const editpostionState = atom({
  key: `editPosition/${v1()}`,
  default: "위도",
});

export const editTitleState = atom({
  key: `editTitle/${v1()}`,
  default: "제목",
});

export const editImageState = atom({
  key: `editImage/${v1()}`,
  default: [],
});

export const editCategoryState = atom({
  key: `editCategory/${v1()}`,
  default: "",
});

export const editRelatedState = atom({
  key: `editRelated/${v1()}`,
  default: "관련태그기본",
});

export const editRelatedAtmasState = atom({
  key: `editAtmos/${v1()}`,
  default: "분위기기본",
});

export const editRelatedPriceState = atom({
  key: `editPrice/${v1()}`,
  default: "가격기본",
});

export const editStarState = atom({
  key: `editStar/${v1()}`,
  default: "0",
});

export const editCommentState = atom({
  key: `editComment/${v1()}`,
  default: "",
});

//post 페이지 정보 (기차역, 주소, 카테고리 태그, 관련 카테고리 태그, 분위기태그, 가격태그)
// 명명구조 (페이지이름 + 정보 + State)
// WriteModal 에서 카테고리 정보 넘기고 PostTrainSelect 모달에서 기차역 정보 선택
