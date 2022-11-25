/*eslint-disable*/
import { useEffect } from "react";
import { useRecoilState, useRecoilValue } from "recoil";
import axios from "axios";
import { useParams } from "react-router-dom";

import Header from "../components/Header";
import CategoryTabs from "../components/MainPage/CategoryTab";
import MainMap from "../components/MainPage/MainMap";
import RelatedTab from "../components/MainPage/TagTab";
import MainHeader from "../components/MainPage/MainHeader";
import WriteModal from "../components/modals/WriteModal";
import TestPostList from "../components/MainPage/Posts/TestPostList";
import { mapCenterMoveEvent, mapImgClickEvent } from "../atoms/mapImage";
import {
  mainPostData,
  mainPageInfo,
  selectCategoryEvent,
  selectTagEvent,
} from "../atoms/mainPageData";
import { trainInfo } from "../atoms/trainInfo";
import { accessToken } from "../atoms/loginTest";
import { tagsInfoToNumList } from "../atoms/tagsInfo";

// 테스트용 입니다. 테스트 완료하면 합칠예정..!
// import PostList from "../components/MainPage/Posts/PostList";
// import DummyPostList from "../components/MainPage/Posts/dummyPostList";

const MainPage = () => {
  // 더미 데이터 통신 될 경우 변경
  const [TOKEN] = useRecoilState(accessToken);
  const { id } = useParams();
  // 필터 && 태그 정보
  const selectCategory = useRecoilValue(selectCategoryEvent);
  const selectTag = useRecoilValue(selectTagEvent);
  const tagsInfoToNum = useRecoilValue(tagsInfoToNumList);

  // Main Map Event 관련 정보
  const [, setMapImgClickid] = useRecoilState(mapImgClickEvent);
  const trainStationInfo = useRecoilValue(trainInfo);
  // Main 게시글 데이터
  const [, setPostList] = useRecoilState(mainPostData);
  // Main 인피니티 스크롤 관련 정보
  const [, setPageInfo] = useRecoilState(mainPageInfo);
  const [, setMapCenter] = useRecoilState(mapCenterMoveEvent);
  console.log("selectTag", tagsInfoToNum[selectTag]);
  // 메인페이지 데이터 통신
  useEffect(() => {
    const config = {
      headers: { Authorization: TOKEN },
    };
    const URL =
      tagsInfoToNum[selectTag] !== 0
        ? `${process.env.REACT_APP_URL}/${id}/${selectCategory}/date/search/?page=1&size=12&tag=${tagsInfoToNum[selectTag]}`
        : `${process.env.REACT_APP_URL}/${id}/${selectCategory}/default/?page=1&size=12`;
    if (TOKEN === "") {
      axios
        .get(URL)
        .then(function (response) {
          //handle success
          // console.log("메인페이지", response);
          setPostList(response.data.items);
          setPageInfo(response.data.pageInfo);
          setMapCenter([trainStationInfo[id - 1].position]);
          setMapImgClickid(null);
        })
        .catch(function (error) {
          //handle error
          console.log(error);
        });
    } else {
      axios
        .get(URL, config)
        .then(function (response) {
          //handle success
          console.log("메인페이지", response);
          setPostList(response.data.items);
          setPageInfo(response.data.pageInfo);
          setMapCenter([trainStationInfo[id - 1].position]);
          setMapImgClickid(null);
        })
        .catch(function (error) {
          //handle error
          console.log(error);
        });
    }
  }, [id, selectCategory, selectTag]);

  // 지도에서 게시글 정보 보이는 기능 초기화
  const [, setMapImgClickId] = useRecoilState(mapImgClickEvent);
  useEffect(() => {
    setMapImgClickId(null);
  }, []);

  return (
    <>
      <Header />
      <div className="flex flex-col justify-center items-center lg:w-full">
        <div className="grid grid-cols-1 lg:grid-cols-2">
          <div>
            <MainHeader />
            <div className="mt-14">
              <MainMap stationId={id} />
            </div>
          </div>
          <div>
            <CategoryTabs />
            <RelatedTab />
            <TestPostList stationId={id} />
          </div>
        </div>
      </div>
      <WriteModal />
    </>
  );
};

export default MainPage;
